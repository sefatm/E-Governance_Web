import { Component, OnInit, OnDestroy } from '@angular/core';
import { Election } from 'src/app/models/election.model';
import { ElectionService } from 'src/app/services/election.service';
import { VoteService } from 'src/app/services/vote.service';
import { LanguageService } from 'src/app/services/language.service';

declare var Chart: any;

@Component({
  selector: 'app-analytics-dashboard',
  templateUrl: './analytics-dashboard.component.html',
  styleUrls: ['./analytics-dashboard.component.css']
})
export class AnalyticsDashboardComponent implements OnInit, OnDestroy {

  elections: Election[] = [];
  selectedElectionId: number | null = null;

  totalVotes          = 0;
  totalCandidates     = 0;
  totalApprovedVoters = 0;
  turnout             = 0;
  results: any[]      = [];
  isLoading           = false;

  barChart:    any = null;
  donutChart:  any = null;

  constructor(public ls: LanguageService, 
    private electionService: ElectionService,
    private voteService:     VoteService
  ) {}

  ngOnInit(): void { this.loadElections(); }

  ngOnDestroy(): void {
    if (this.barChart)   this.barChart.destroy();
    if (this.donutChart) this.donutChart.destroy();
  }

  loadElections() {
    this.electionService.getAll().subscribe({
      next: (res) => this.elections = res,
      error: (err) => console.error(err)
    });
  }

  loadAnalytics() {
    if (!this.selectedElectionId) return;
    this.isLoading = true;

    this.voteService.getAnalytics(this.selectedElectionId).subscribe({
      next: (res: any) => {
        this.isLoading           = false;
        this.results             = res.results             || [];
        this.totalVotes          = res.totalVotes          || 0;
        this.totalCandidates     = res.totalCandidates     || 0;
        this.totalApprovedVoters = res.totalApprovedVoters || 0;
        this.turnout             = res.turnoutPercent      || 0;
        setTimeout(() => this.renderCharts(), 100);
      },
      error: (err) => { this.isLoading = false; console.error(err); }
    });
  }

  renderCharts() {
    if (this.barChart)   { this.barChart.destroy();   this.barChart   = null; }
    if (this.donutChart) { this.donutChart.destroy();  this.donutChart = null; }

    const tooltip = { backgroundColor: '#0a3d1f', titleColor: '#fff', bodyColor: '#a5c8b0', padding: 12, cornerRadius: 8 };

    const barCanvas = document.getElementById('barChart') as HTMLCanvasElement;
    if (barCanvas && typeof Chart !== 'undefined') {
      this.barChart = new Chart(barCanvas, {
        type: 'bar',
        data: {
          labels: this.results.map(r => r.name),
          datasets: [{
            label: 'Votes',
            data: this.results.map(r => r.votes),
            backgroundColor: '#0f7a3f',
            borderRadius: 7,
            barPercentage: 0.6
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { display: false }, tooltip },
          scales: {
            x: { grid: { display: false }, ticks: { color: '#5a7a5a', font: { size: 12 } } },
            y: { grid: { color: 'rgba(10,87,52,.07)' }, ticks: { color: '#5a7a5a', font: { size: 12 }, stepSize: 1 }, beginAtZero: true }
          }
        }
      });
    }

    const donutCanvas = document.getElementById('donutChart') as HTMLCanvasElement;
    if (donutCanvas && typeof Chart !== 'undefined') {
      this.donutChart = new Chart(donutCanvas, {
        type: 'doughnut',
        data: {
          labels: ['Voted', 'Not Voted'],
          datasets: [{
            data: [this.turnout, Math.max(0, 100 - this.turnout)],
            backgroundColor: ['#0f7a3f', '#e0e8e0'],
            borderWidth: 0,
            hoverOffset: 5
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          cutout: '72%',
          plugins: { legend: { display: false }, tooltip }
        }
      });
    }
  }
}
