package com.example.githubapi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class GithubApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(GithubApiApplication.class, args);
	}
}

@RestController
class GithubController {

	private final String GITHUB_API_BASE_URL = "https://api.github.com";
	private final RestTemplate restTemplate = new RestTemplate();

	@GetMapping("/repositories/{username}")
	public ResponseEntity<Object> getRepositories(
			@PathVariable String username,
			@RequestHeader("Accept") String acceptHeader
	) {
		String url = GITHUB_API_BASE_URL + "/users/" + username + "/repos";

		try {
			ResponseEntity<Map[]> response = restTemplate.getForEntity(url, Map[].class);
			Map[] repositories = response.getBody();
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

			if ("application/xml".equals(acceptHeader)) {
				return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
						.body(new ErrorResponse(HttpStatus.NOT_ACCEPTABLE.value(), "Unsupported media type"));
			}

			return ResponseEntity.ok(filteredRepositories);
		} catch (HttpClientErrorException.NotFound notFoundException) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "User not found"));
		}
	}

	private String getLastCommitSha(String ownerLogin, String repoName, String branchName) {
		String url = GITHUB_API_BASE_URL + "/repos/" + ownerLogin + "/" + repoName + "/commits/" + branchName;
		ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
		Map commitInfo = response.getBody();
		return (String) commitInfo.get("sha");
	}
}

class RepositoryInfo {
	private String repositoryName;
	private String ownerLogin;
	private List<BranchInfo> branches;

	public RepositoryInfo(String repositoryName, String ownerLogin, List<BranchInfo> branches) {
		this.repositoryName = repositoryName;
		this.ownerLogin = ownerLogin;
		this.branches = branches;
	}

	public String getRepositoryName() {
		return repositoryName;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public String getOwnerLogin() {
		return ownerLogin;
	}

	public void setOwnerLogin(String ownerLogin) {
		this.ownerLogin = ownerLogin;
	}

	public List<BranchInfo> getBranches() {
		return branches;
	}

	public void setBranches(List<BranchInfo> branches) {
		this.branches = branches;
	}
}

class BranchInfo {
	private String branchName;
	private String lastCommitSha;

	public BranchInfo(String branchName, String lastCommitSha) {
		this.branchName = branchName;
		this.lastCommitSha = lastCommitSha;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getLastCommitSha() {
		return lastCommitSha;
	}

	public void setLastCommitSha(String lastCommitSha) {
		this.lastCommitSha = lastCommitSha;
	}
}

class ErrorResponse {
	private int status;
	private String message;

	public ErrorResponse(int status, String message) {
		this.status = status;
		this.message = message;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
