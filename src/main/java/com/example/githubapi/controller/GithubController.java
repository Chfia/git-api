package com.example.githubapi.controller;

import com.example.githubapi.dto.RepositoryInfo;
import com.example.githubapi.exception.UserNotFoundException;
import com.example.githubapi.service.GithubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import com.example.githubapi.dto.BranchInfo;


import java.util.List;

@RestController
public class GithubController {

    private final GithubService githubService;

    @Autowired
    public GithubController(GithubService githubService) {
        this.githubService = githubService;
    }

    @GetMapping("/repositories/{username}")
    public ResponseEntity<Object> getRepositories(
            @PathVariable String username,
            @RequestHeader("Accept") String acceptHeader
    ) {
        try {
            List<RepositoryInfo> filteredRepositories = githubService.getNonForkRepositories(username);

            if ("application/xml".equals(acceptHeader)) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                        .body("Unsupported media type");
            }

            return ResponseEntity.ok(filteredRepositories);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }
    }
}
