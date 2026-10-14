package com.forknight.server.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ai_reviews")
public class AiReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pull_request_id")
    private PullRequest pullRequest;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "review_body", columnDefinition = "TEXT")
    private String reviewBody;

    @Column(nullable = false)
    private int score;

    @Column(name = "issues_json", columnDefinition = "TEXT")
    private String issuesJson;

    @Column(name = "model_used", nullable = false, length = 50)
    private String modelUsed;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AiReview() {}

    public AiReview(PullRequest pullRequest, String summary, String reviewBody, int score, String issuesJson, String modelUsed, Integer tokensUsed) {
        this.pullRequest = pullRequest;
        this.summary = summary;
        this.reviewBody = reviewBody;
        this.score = score;
        this.issuesJson = issuesJson;
        this.modelUsed = modelUsed;
        this.tokensUsed = tokensUsed;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PullRequest getPullRequest() { return pullRequest; }
    public void setPullRequest(PullRequest pullRequest) { this.pullRequest = pullRequest; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getReviewBody() { return reviewBody; }
    public void setReviewBody(String reviewBody) { this.reviewBody = reviewBody; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getIssuesJson() { return issuesJson; }
    public void setIssuesJson(String issuesJson) { this.issuesJson = issuesJson; }

    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }

    public Integer getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(Integer tokensUsed) { this.tokensUsed = tokensUsed; }

    public Instant getCreatedAt() { return createdAt; }
}
