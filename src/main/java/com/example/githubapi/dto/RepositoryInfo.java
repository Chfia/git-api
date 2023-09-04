package com.example.githubapi.dto;

import java.util.List;

public class RepositoryInfo {

    private String repositoryName;
    private String ownerLogin;
    private List<BranchInfo> branches;

    public RepositoryInfo(String repoName, String ownerLogin, List<BranchInfo> branchInfoList) {
    }

    // Constructors, getters, and setters
}
