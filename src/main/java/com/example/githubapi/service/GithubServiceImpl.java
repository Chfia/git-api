package com.example.githubapi.service;

import com.example.githubapi.dto.RepositoryInfo;
import com.example.githubapi.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.githubapi.dto.BranchInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GithubServiceImpl implements GithubService {

    private static final String GITHUB_API_BASE_URL = "https://api.github.com";
    private final RestTemplate restTemplate;

    @Autowired
    public GithubServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<RepositoryInfo> getNonForkRepositories(String username) {
        String url = GITHUB_API_BASE_URL + "/users/" + username + "/repos";

        ResponseEntity<Map[]> response = restTemplate.getForEntity(url, Map[].class);
        Map[] repositories = response.getBody();

        if (repositories == null || repositories.length == 0) {
            throw new UserNotFoundException("User not found");
        }

        List<RepositoryInfo> filteredRepositories = new ArrayList<>();

        for (Map repository : repositories) {
            if (!(boolean) repository.get("fork")) {
                String repoName = (String) repository.get("name");
                String ownerLogin = (String) ((Map) repository.get("owner")).get("login");

                String branchesUrl = (String) repository.get("branches_url");
                branchesUrl = branchesUrl.replace("{/branch}", "");
                ResponseEntity<Map[]> branchesResponse = restTemplate.getForEntity(branchesUrl, Map[].class);
                Map[] branches = branchesResponse.getBody();

                List<BranchInfo> branchInfoList = new ArrayList<>();
                for (Map branch : branches) {
                    String branchName = (String) branch.get("name");
                    String lastCommitSha = getLastCommitSha(ownerLogin, repoName, branchName);
                    branchInfoList.add(new BranchInfo(branchName, lastCommitSha));
                }


                filteredRepositories.add(new RepositoryInfo(repoName, ownerLogin, branchInfoList));
            }
        }

        return filteredRepositories;
    }

    private String getLastCommitSha(String ownerLogin, String repoName, String branchName) {
        String url = GITHUB_API_BASE_URL + "/repos/" + ownerLogin + "/" + repoName + "/commits/" + branchName;
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map commitInfo = response.getBody();
        return (String) commitInfo.get("sha");
    }
}
