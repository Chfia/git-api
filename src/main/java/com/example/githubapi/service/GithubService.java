package com.example.githubapi.service;

import com.example.githubapi.dto.RepositoryInfo;

import java.util.List;

public interface GithubService {

    List<RepositoryInfo> getNonForkRepositories(String username);
}
