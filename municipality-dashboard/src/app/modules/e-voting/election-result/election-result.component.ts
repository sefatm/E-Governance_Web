import { environment } from 'src/environments/environment';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { Election } from 'src/app/models/election.model';
import { VoteResult } from 'src/app/models/vote.model';
import { ElectionService } from 'src/app/services/election.service';
import { VoteService } from 'src/app/services/vote.service';
import { LanguageService } from 'src/app/services/language.service';

declare var Chart: any;

@Component({
  selector: 'app-election-result',
  templateUrl: './election-result.component.html',
  styleUrls: ['./election-result.component.css']
})
export class ElectionResultComponent implements OnInit, OnDestroy {
  readonly serverUrl = environment.serverUrl;

  elections:          Election[]   = [];
  results:            VoteResult[] = [];
  selectedElectionId: number | null = null;
  selectedName:       string = '';

  chart:     any = null;
  isLoading  = false;
  winner:    VoteResult | null = null;
  totalVotes = 0;

  constructor(public ls: LanguageService, 
    private electionService: ElectionService,
    private voteService:     VoteService
  ) {}

  ngOnInit(): void { this.loadElections(); }

  ngOnDestroy(): void { if (this.chart) this.chart.destroy(); }

  loadElections() {
    this.electionService.getAll().subscribe({
      next: (res) => this.elections = res,
      error: (err) => console.error(err)
    });
  }

  loadResult() {
    if (!this.selectedElectionId) return;
    this.isLoading = true;
    this.results   = [];
    this.winner    = null;
    this.totalVotes = 0;

    const el = this.elections.find(e => e.id == this.selectedElectionId);
    this.selectedName = el?.name || '';

    this.voteService.getResult(this.selectedElectionId).subscribe({
      next: (res: any[]) => {
        this.isLoading  = false;
        this.results    = res;
        this.totalVotes = res.reduce((sum, r) => sum + (r.votes || 0), 0);
        
        if (res.length > 0) {
          this.winner = res.reduce((p, c) => c.votes > p.votes ? c : p);
        }
        setTimeout(() => this.renderChart(), 100);
      },
      error: (err) => { this.isLoading = false; console.error(err); }
    });
  }

  renderChart() {
    if (this.chart) { this.chart.destroy(); this.chart = null; }

    const canvas = document.getElementById('resultChart') as HTMLCanvasElement;
    if (!canvas || typeof Chart === 'undefined') return;

    const colors = ['#0f7a3f','#1565C0','#E65100','#6A1B9A','#C62828','#00695C','#4527A0','#37474F'];

    this.chart = new Chart(canvas, {
      type: 'bar',
      data: {
        labels: this.results.map(r => r.name),
        datasets: [{
          label: 'Votes',
          data:   this.results.map(r => r.votes),
          backgroundColor: colors.slice(0, this.results.length),
          borderRadius: 7,
          barPercentage: 0.6
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            backgroundColor: '#0a3d1f',
            titleColor: '#fff',
            bodyColor: '#a5c8b0',
            padding: 12,
            cornerRadius: 8
          }
        },
        scales: {
          x: { grid: { display: false }, ticks: { color: '#5a7a5a', font: { size: 12 } } },
          y: { grid: { color: 'rgba(10,87,52,.07)' }, ticks: { color: '#5a7a5a', font: { size: 12 }, stepSize: 1 }, beginAtZero: true }
        }
      }
    });
  }
}
