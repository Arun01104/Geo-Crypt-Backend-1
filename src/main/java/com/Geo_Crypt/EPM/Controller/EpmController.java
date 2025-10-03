package com.Geo_Crypt.EPM.Controller;

import com.Geo_Crypt.EPM.Dto.AuthRequest;
import com.Geo_Crypt.EPM.Dto.AuthResponse;
import com.Geo_Crypt.EPM.Dto.PolygonDto;
import com.Geo_Crypt.EPM.Entity.ExamPaper;
import com.Geo_Crypt.EPM.Entity.Polygon;
import com.Geo_Crypt.EPM.Enum.Role;
import com.Geo_Crypt.EPM.Repository.UserRepository;
import com.Geo_Crypt.EPM.Services.EpmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/epm")
@RequiredArgsConstructor
public class EpmController {
    private final UserRepository userrepo;
    private final EpmService service;
    private final org.springframework.security.crypto.password.PasswordEncoder encoder;

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestParam String username, @RequestParam String password, @RequestParam String email, @RequestParam Role role){
        service.registerUser(username,password,email,role,encoder);
        return ResponseEntity.ok("registered");
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req){
        String token = service.login(req.getUsername(), req.getPassword(),encoder);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/polygons")
    public ResponseEntity<?> createPolygon(@RequestBody PolygonDto dto){

        PolygonDto p = service.createPolygon(dto);
        return ResponseEntity.ok(p);
    }

    @PostMapping(value = "/exams/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadExam(@RequestParam("file") MultipartFile file, @RequestParam("polygonId") Long polygonId, @RequestParam("start") String start, @RequestParam("end") String end, @RequestParam("threshold") int threshold) throws Exception {
        ZoneId istZone = ZoneId.of("Asia/Kolkata");

        ExamPaper p = service.uploadExam(file, polygonId,LocalDateTime.parse(start) , LocalDateTime.parse(end), threshold);
        return ResponseEntity.ok(p);
    }

    @GetMapping(value = "/exams/{id}/download")
    public ResponseEntity<?> downloadExam(@PathVariable Long id, @RequestParam double lat, @RequestParam double lon) throws Exception {
        byte[] pdf = service.fetchDecryptedPaper(id, lat, lon);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=exam_"+id+".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}
