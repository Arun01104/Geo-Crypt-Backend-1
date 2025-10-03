package com.Geo_Crypt.EPM.Services;

import com.Geo_Crypt.EPM.Config.JwtUtil;
import com.Geo_Crypt.EPM.Dto.EncryptionResponse;
import com.Geo_Crypt.EPM.Dto.PolygonDto;
import com.Geo_Crypt.EPM.Dto.VertexDto;
import com.Geo_Crypt.EPM.Entity.*;
import com.Geo_Crypt.EPM.Enum.Role;
import com.Geo_Crypt.EPM.Repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EpmService {
    private final UserRepository userRepo;
    private final PolygonRepository polygonRepo;
    private final ExamPaperRepository examRepo;
    private final VertexRepository vertexRepo;
    private final JwtUtil jwtUtil;
    private final CryptoShareRefRepository cryptoShareRefRepo;

    private ObjectMapper objectMapper = new ObjectMapper();

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${crypto.service.url}")
    private  String cryptoServiceUrl;

    public void registerUser(String username, String rawPassword, String email, Role role, org.springframework.security.crypto.password.PasswordEncoder encoder){
        if(userRepo.findByUsername(username).isPresent()) throw new RuntimeException("User exists");
        User u = User.builder().username(username).password(encoder.encode(rawPassword)).email(email).role(role).build();
        userRepo.save(u);
        //return "ok";
    }

    public String login(String username, String password, org.springframework.security.crypto.password.PasswordEncoder encoder){
        User u = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User Not Found"));
        if(!encoder.matches(password, u.getPassword())) throw new RuntimeException(" Password Invalid");
        return jwtUtil.generateToken(u.getUsername(), u.getRole().name());
    }

    @Transactional
    public PolygonDto createPolygon(PolygonDto dto){
        Polygon p= new Polygon();
        p.setName(dto.getName());
        List<Vertex> vertices = new ArrayList<>();
        for(VertexDto v : dto.getVertices()){
            Vertex vertex= Vertex.builder()
                    .idx(v.idx)
                    .latitude(v.latitude)
                    .longitude(v.longitude)
                    .polygon(p)
                    .build();
            vertices.add(vertex);
        }
        p.setVertices(vertices);
        Polygon saved = polygonRepo.save(p);
        List<VertexDto> vr = saved.getVertices().stream()
                .map(x-> new VertexDto(x.getIdx(),x.getLatitude(),x.getLongitude())).toList();
        return new PolygonDto(saved.getName(), vr);
    }

    /*public ExamPaper uploadExam(MultipartFile file, Long polygonId, Instant start,Instant end,int threshold) throws Exception{
        Polygon polygon = polygonRepo.findById(polygonId).orElseThrow(()-> new RuntimeException("Polygon not found"));
        List<Integer> verticesIdx = polygon.getVertices().stream().sorted(Comparator.comparing(Vertex::getIdx)).map(Vertex::getIdx).collect(Collectors.toList());

        String url = cryptoServiceUrl+"/upload";
        MultiValueMap<String,Object> body = new LinkedMultiValueMap<>();

        ByteArrayResource fileAsResource = new ByteArrayResource(file.getBytes()){
            @Override
            public String getFilename(){return file.getOriginalFilename();}
        };
        body.add("file",fileAsResource);
        body.add("polygonId",String.valueOf(polygonId));
        body.add("start",start.toString());
        body.add("end",end.toString());
        body.add("threshold",threshold);
        body.add("vertices",verticesIdx);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String ,Object>> requestEntity = new HttpEntity<>(body,headers);

        ResponseEntity<Map> resp = restTemplate.postForEntity(url,requestEntity,Map.class);
        Map respBody = resp.getBody();
        Number cryptoPaperId = (Number) respBody.get("id");
        ExamPaper paper = ExamPaper.builder().title(file.getOriginalFilename()).subject("unknown").examStart(start).examEnd(end).polygon(polygon).cryptoPaperId(cryptoPaperId.longValue()).build();
        examRepo.save(paper);
        return paper;
    }*/

    public ExamPaper uploadExam(MultipartFile file, Long polygonId, LocalDateTime  start, LocalDateTime  end, int threshold) throws Exception {
        Polygon polygon = polygonRepo.findById(polygonId).orElseThrow(() -> new RuntimeException("polygon not found"));
        List<Vertex> sortedVertices = polygon.getVertices().stream()
                .sorted(Comparator.comparing(Vertex::getIdx))
                .collect(Collectors.toList());
        List<Integer> verticesIdx = sortedVertices.stream().map(Vertex::getIdx).collect(Collectors.toList());
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        // 2. create and save exam paper in EPM DB first
        ExamPaper paper = ExamPaper.builder()
                .title(file.getOriginalFilename())
                .subject("unknown")
                .examStart(start)
                .examEnd(end)
                .polygon(polygon)
                .threshold(threshold)
                .build();
        paper = examRepo.save(paper);

        // 3. build multipart request to Crypto Service
        String url = cryptoServiceUrl + "/upload";
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        ByteArrayResource fileAsResource = new ByteArrayResource(file.getBytes()){
            @Override public String getFilename() { return file.getOriginalFilename(); }
        };
        body.add("file", fileAsResource);
        body.add("paperId", String.valueOf(paper.getId()));
        body.add("vertices", objectMapper.writeValueAsString(verticesIdx)); // RestTemplate will serialize each list element as parameter
        body.add("threshold", threshold);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String,Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<EncryptionResponse> resp = restTemplate.postForEntity(url, requestEntity, EncryptionResponse.class);
        //if (!resp.getStatusCode().is2xxSuccessful()) throw new RuntimeException("crypto service error: " + resp.getStatusCode());

        // 4. parse response (EncryptionResponse JSON)
        // Example response expected to have vertexShareMapping as object
        EncryptionResponse respBody = resp.getBody();
        if(respBody == null) throw new RuntimeException("Crypto response is null");

       // Map<String, Object> respMap = objectMapper.readValue(resp.getBody(), Map.class);

        String minioObjectName = respBody.getMinioObjectName();
        String ivHex = respBody.getIvHex();
        Map<Integer, Long> vertexShareMappingRaw = respBody.getVertexShareMapping();

        // 5. persist mapping in EPM DB: vertexIndex -> cryptoShareId
        for (Map.Entry<Integer, Long> entry : vertexShareMappingRaw.entrySet()) {
            Integer vertexIndex = entry.getKey();
            Long cryptoShareId = entry.getValue().longValue();

            // find the Vertex by polygonId and idx
            Vertex v = vertexRepo.findByPolygonIdAndIdx(polygon.getId(),vertexIndex)
                    .orElseThrow(() -> new RuntimeException("vertex not found for idx "+vertexIndex));

            CryptoShareRef ref = CryptoShareRef.builder()
                    .examPaper(paper)
                    .vertex(v)
                    .cryptoShareId(cryptoShareId)
                    .build();
            cryptoShareRefRepo.save(ref);
        }

        paper.setTitle(paper.getTitle());

        examRepo.save(paper);

        return paper;
    }

    public byte[] fetchDecryptedPaper(Long examPaperId, double lat, double lon) throws Exception {
        ExamPaper paper = examRepo.findById(examPaperId).orElseThrow(() -> new RuntimeException("exam not found"));
        LocalDateTime now = LocalDateTime.now();

        if(now.isBefore(paper.getExamStart()) || now.isAfter(paper.getExamEnd())) throw new RuntimeException("Not in time window");

        // Point-in-polygon check (ray casting) using polygon vertices
        boolean inside = isPointInPolygon(lat, lon, paper.getPolygon().getVertices());
        if(!inside) throw new RuntimeException("Not inside polygon");

        // Build list of vertex indices (we choose first K vertices here; in production choose K distributed vertices)
        List<CryptoShareRef> refs = cryptoShareRefRepo.findByExamPaperId(paper.getId());
        List<Integer> availableIndices = refs.stream().map(r -> r.getVertex().getIdx()).collect(Collectors.toList());

        // Call crypto decrypt endpoint
        int k = paper.getThreshold();
        List<Integer> indicesToSend = availableIndices.subList(0,Math.min(k,availableIndices.size()));
        String url = cryptoServiceUrl + "/decrypt/" + paper.getId();
        HttpHeaders headers = new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<Integer>> req = new HttpEntity<>(indicesToSend, headers);
        ResponseEntity<byte[]> resp = restTemplate.postForEntity(url, req, byte[].class);
        if(!resp.getStatusCode().is2xxSuccessful()) throw new RuntimeException("crypto error");
        return resp.getBody();
    }
    private boolean isPointInPolygon(double lat, double lon, List<Vertex> vertices){
        boolean inside = false;
        for (int i = 0, j = vertices.size() - 1; i < vertices.size(); j = i++) {
            double xi = vertices.get(i).getLongitude(), yi = vertices.get(i).getLatitude();
            double xj = vertices.get(j).getLongitude(), yj = vertices.get(j).getLatitude();
            double epsilon = 1e-10;
            boolean intersect = ((yi > lat) != (yj > lat)) &&
                    (lon < (xj - xi) * (lat - yi) / ((yj - yi) + epsilon) + xi);

            if (intersect) inside = !inside;
        }
        return inside;
    }


}
