package com.mgt.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vote_cast", uniqueConstraints = @UniqueConstraint(name = "UKvote_voter_election", columnNames = {"voter_id", "election_id"}))
@NoArgsConstructor
@AllArgsConstructor
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "election_id", foreignKey = @ForeignKey(name = "fk_vote_election"))
    private Election election;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", foreignKey = @ForeignKey(name = "fk_vote_candidate"))
    private Nominee candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voter_id", foreignKey = @ForeignKey(name = "fk_vote_voter"))
    private VoterRegistration voter;

    private String status;

    @Column(name = "casted_at")
    private LocalDateTime castedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.castedAt  = now;
        if (this.status == null) this.status = "Casted";
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Election getElection() {
		return election;
	}

	public void setElection(Election election) {
		this.election = election;
	}

	public Nominee getCandidate() {
		return candidate;
	}

	public void setCandidate(Nominee candidate) {
		this.candidate = candidate;
	}

	public VoterRegistration getVoter() {
		return voter;
	}

	public void setVoter(VoterRegistration voter) {
		this.voter = voter;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getCastedAt() {
		return castedAt;
	}

	public void setCastedAt(LocalDateTime castedAt) {
		this.castedAt = castedAt;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

    
}
