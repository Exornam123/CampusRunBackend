package com.example.campusrunbackend.dto;

import com.example.campusrunbackend.model.Attachment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProviderAdminDetailDto {
    private Long id;
    private Long userId;
    private String name;
    private String username;
    private String email;
    private String phoneNumber;
    private String indexNumber;
    private String bio;
    private String location;
    private List<String> categories;
    private java.util.Collection<Attachment> cvFiles;
    private java.util.Collection<Attachment> proofFiles;
    private String status;
}
