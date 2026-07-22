import { Component, HostListener, OnInit } from '@angular/core';
import { LanguageService } from 'src/app/services/language.service';

@Component({
  selector: 'app-main-layout',
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.css']
})
export class MainLayoutComponent implements OnInit {

  isCollapsed  = false;
  isMobileOpen = false;
  isMobile     = false;

  constructor(public ls: LanguageService) {}

  ngOnInit(): void {
    this.checkScreen();
  }

  @HostListener('window:resize')
  checkScreen() {
    const wasMobile = this.isMobile;
    this.isMobile = window.innerWidth <= 768;

    if (!this.isMobile) {
      // Desktop এ mobile overlay বন্ধ করো
      this.isMobileOpen = false;
    }

    // Mobile → Desktop transition: sidebar collapse state reset করো
    if (wasMobile && !this.isMobile) {
      this.isCollapsed = false;
    }
  }

  toggleSidebar() {
    if (this.isMobile) {
      this.isMobileOpen = !this.isMobileOpen;
    } else {
      this.isCollapsed = !this.isCollapsed;
    }
  }
}
