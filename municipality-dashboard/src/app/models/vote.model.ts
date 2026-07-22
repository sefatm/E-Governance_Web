export interface VoteCast {
  electionId:  number;
  candidateId: number;
  voterId:     number;
}

export interface VoteResult {
  candidateId: number;
  name:        string;
  party:       string;
  votes:       number;
  percent?:    number;
  symbol?: string;
  symbolFileUrl?: string;
}

export interface AnalyticsResponse {
  totalVotes:          number;
  totalCandidates:     number;
  totalApprovedVoters: number;
  turnoutPercent:      number;
  results:             VoteResult[];
}
