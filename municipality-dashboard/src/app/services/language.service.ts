import { GLOBAL_EN_BN } from './global-language-map';
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type Lang = 'en' | 'bn';

@Injectable({ providedIn: 'root' })
export class LanguageService {
  private observer?: MutationObserver;
  private isTranslating = false;
  private translateScheduled = false;
  private pendingRoots = new Set<Node>();
  private originalText = new WeakMap<Node, string>();
  private originalAttr = new WeakMap<Element, Record<string, string>>();
  private readonly bnToEn: Record<string, string> = Object.keys(GLOBAL_EN_BN).reduce(
    (acc: Record<string, string>, en: string) => {
      acc[GLOBAL_EN_BN[en]] = en;
      return acc;
    }, {}
  );
  private readonly enPhrases = Object.keys(GLOBAL_EN_BN).sort((a, b) => b.length - a.length);
  private readonly bnPhrases = Object.keys(this.bnToEn).sort((a, b) => b.length - a.length);

  constructor() {
    setTimeout(() => this.applyToDocument(), 0);
  }

  private _lang = new BehaviorSubject<Lang>(
    (localStorage.getItem('app_lang') as Lang) || 'en'
  );

  lang$ = this._lang.asObservable();

  get current(): Lang { return this._lang.value; }

  toggle() {
    const next: Lang = this._lang.value === 'en' ? 'bn' : 'en';
    this._lang.next(next);
    localStorage.setItem('app_lang', next);
    this.applyToDocument();
  }

  set(lang: Lang) {
    this._lang.next(lang);
    localStorage.setItem('app_lang', lang);
    this.applyToDocument();
  }


  private startDomTranslation(): void {
    this.applyToDocument();
  }

  private applyToDocument(): void {
    if (typeof document === 'undefined' || !document.body) return;
    document.documentElement.lang = this.current === 'bn' ? 'bn' : 'en';
    document.body.setAttribute('data-app-lang', this.current);
  }

  private queueTranslate(root: Node): void {
    if (root.nodeType === Node.TEXT_NODE || root instanceof Element) {
      this.pendingRoots.add(root);
    }
    if (this.translateScheduled) return;
    this.translateScheduled = true;
    requestAnimationFrame(() => {
      const roots = Array.from(this.pendingRoots);
      this.pendingRoots.clear();
      this.translateScheduled = false;
      this.isTranslating = true;
      try {
        roots.forEach(node => this.translateTree(node));
      } finally {
        this.isTranslating = false;
      }
    });
  }

  private translateTree(root: Node): void {
    if (root.nodeType === Node.TEXT_NODE) {
      this.translateTextNode(root);
      return;
    }
    if (!(root instanceof Element)) return;

    const tag = root.tagName.toLowerCase();
    if (tag === 'script' || tag === 'style' || tag === 'code' || tag === 'pre') return;

    this.translateAttributes(root);
    const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT);
    let node: Node | null = walker.nextNode();
    while (node) {
      const parent = node.parentElement?.tagName?.toLowerCase();
      if (!parent || !['script', 'style', 'code', 'pre'].includes(parent)) {
        this.translateTextNode(node);
      }
      node = walker.nextNode();
    }
    root.querySelectorAll('*').forEach(el => this.translateAttributes(el));
  }

  private translateTextNode(node: Node): void {
    const raw = node.nodeValue ?? '';
    const trimmed = raw.trim();
    if (!trimmed) return;

    if (!this.originalText.has(node)) {
      this.originalText.set(node, trimmed);
    } else {
      const known = this.originalText.get(node)!;
      const translatedKnown = GLOBAL_EN_BN[known] || this.bnToEn[known];
      if (!translatedKnown && raw.trim() !== known) this.originalText.set(node, raw.trim());
    }

    const original = this.originalText.get(node) ?? trimmed;
    const translated = this.translateLiteral(original);
    if (translated !== trimmed) {
      const leading = raw.match(/^\s*/)?.[0] ?? '';
      const trailing = raw.match(/\s*$/)?.[0] ?? '';
      node.nodeValue = `${leading}${translated}${trailing}`;
    }
  }

  private translateAttributes(el: Element): void {
    const attrs = ['placeholder', 'title', 'aria-label', 'alt'];
    let originals = this.originalAttr.get(el);
    if (!originals) {
      originals = {};
      this.originalAttr.set(el, originals);
    }
    for (const attr of attrs) {
      if (!el.hasAttribute(attr)) continue;
      const current = el.getAttribute(attr) ?? '';
      if (!(attr in originals)) originals[attr] = current;
      const source = originals[attr];
      const translated = this.translateLiteral(source);
      if (translated !== current) el.setAttribute(attr, translated);
    }
  }

  private translateLiteral(value: string): string {
    const v = value.trim();
    if (!v) return value;

    // Exact lookup first for natural, reviewed UI translations.
    if (this.current === 'bn') {
      const exact = GLOBAL_EN_BN[v];
      if (exact) return exact;
      return this.replacePhrases(v, this.enPhrases, GLOBAL_EN_BN);
    }

    if (/^[\x00-\x7F]*$/.test(v)) return value;

    const exact = this.bnToEn[v];
    if (exact) return exact;
    return this.replacePhrases(v, this.bnPhrases, this.bnToEn);
  }

  private replacePhrases(value: string, phrases: string[], dictionary: Record<string, string>): string {
    let out = value;
    for (const source of phrases) {
      if (source.length < 3 || !out.includes(source)) continue;

      // Avoid corrupting words by replacing short ASCII keys such as "No", "Add" or "Day"
      // inside larger words. Bengali phrases can be replaced directly.
      if (/^[\x00-\x7F]+$/.test(source)) {
        const escaped = source.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        const re = new RegExp(`(^|[^A-Za-z0-9])(${escaped})(?=$|[^A-Za-z0-9])`, 'g');
        out = out.replace(re, (_m, prefix) => `${prefix}${dictionary[source]}`);
      } else {
        out = out.split(source).join(dictionary[source]);
      }
    }

    if (this.current === 'en') {
      const bnDigits = '০১২৩৪৫৬৭৮৯';
      out = out.replace(/[০-৯]/g, d => String(bnDigits.indexOf(d)));
      out = out.replace(/\s+টি(?=\s|$|[.,:;!?৳)])/g, '');
    }
    return out;
  }

  t(key: string): string {
    const dict = TRANSLATIONS[this._lang.value];
    return dict?.[key] ?? TRANSLATIONS['en'][key] ?? key;
  }
}

// ─────────────────────────────────────────────────────────────
//  TRANSLATIONS
// ─────────────────────────────────────────────────────────────
const TRANSLATIONS: Record<Lang, Record<string, string>> = {
  en: {
    // ── Navbar ──
    'nav.eGovernance'        : 'E-Governance',
    'nav.dashboard'          : 'Dashboard',
    'nav.search'             : 'Search...',
    'nav.searching'          : 'Searching...',
    'nav.nothingFound'       : 'Nothing found for this search',
    'nav.notifications'      : 'Notifications',
    'nav.noNotifications'    : 'No notifications available',
    'nav.loading'            : 'Loading...',
    'nav.seeAll'             : 'see all →',
    'nav.profile'            : 'Profile',
    'nav.settings'           : 'Settings',
    'nav.logout'             : 'Logout',
    'nav.new'                : 'New',
    // ── Sidebar sections ──
    'sb.services'            : 'Services',
    'sb.dashboard'           : 'Dashboard',
    'sb.citizenService'      : 'Citizen Service',
    'sb.tradeLicense'        : 'Trade License',
    'sb.holdingTax'          : 'Holding Tax',
    'sb.complaint'           : 'Complaint',
    'sb.infrastructure'      : 'Infrastructure',
    'sb.healthSanitation'    : 'Health & Sanitation',
    'sb.project'             : 'Project',
    'sb.waterSupply'         : 'Water Supply',
    'sb.wasteManagement'     : 'Waste Management',
    'sb.payment'             : 'Payment',
    'sb.reports'             : 'Reports',
    'sb.election'            : 'E-Voting',
    'sb.etender'             : 'E-Tender',
    'sb.communication'       : 'Communication',
    'sb.socialCards'         : 'Social Cards',
    'sb.maps'                : 'Maps',
    'sb.notices'             : 'Notices',
    'sb.admin'               : 'Administration',
    'sb.farmDistribution'    : 'Farm Distribution',
    'sb.employee'            : 'Employee',
    // ── Citizen Service ──
    'cs.birthDeath'          : 'Birth / Death Certificate',
    'cs.citizenCert'         : 'Citizen Certificate',
    'cs.familyCert'          : 'Family Certificate',
    'cs.passport'            : 'Passport',
    'cs.appStatus'           : 'Application Status',
    'cs.passportAdmin'       : 'Passport Admin',
    'cs.allApplyData'        : 'All Apply Data',
    // ── Trade License ──
    'tl.applyLicense'        : 'Apply for License',
    'tl.renewal'             : 'License Renewal',
    'tl.statusCheck'         : 'Status Check',
    'tl.allApplyData'        : 'All Apply Data',
    // ── Holding Tax ──
    'ht.newRegistration'     : 'New Registration',
    'ht.taxAssessment'       : 'Tax Assessment',
    'ht.taxPayment'          : 'Tax Payment',
    'ht.taxDueList'          : 'Tax Due List',
    'ht.ownershipTransfer'   : 'Ownership Transfer',
    'ht.collectionReport'    : 'Collection Report',
    'ht.taxApplications'     : 'Tax Applications',
    // ── Complaint ──
    'cp.submit'              : 'Submit Complaint',
    'cp.allComplaints'       : 'All Complaints',
    'cp.tracking'            : 'Tracking',
    'cp.resolution'          : 'Resolution',
    // ── Infrastructure ──
    'inf.road'               : 'Road Maintenance',
    'inf.drainage'           : 'Drainage',
    'inf.streetLight'        : 'Street Light',
    'inf.construction'       : 'Construction Permission',
    'inf.status'             : 'Infrastructure Status',
    // ── Health ──
    'hl.notices'             : 'Health Notices',
    'hl.epiRegister'         : 'EPI Registration',
    'hl.epiAdmin'            : 'EPI Admin',
    'hl.sanitation'          : 'Sewage Monitoring',
    'hl.centerInfo'          : 'Health Center Information',
    // ── Project ──
    'pj.list'                : 'Project List',
    'pj.budget'              : 'Project Budget',
    // ── Water ──
    'wt.connection'          : 'Water Connection',
    'wt.bill'                : 'Water Bill',
    'wt.usageReport'         : 'Water Usage Report',
    'wt.wardList'            : 'Ward List',
    'wt.population'          : 'Population',
    // ── Waste ──
    'ws.schedule'            : 'Garbage Schedule',
    'ws.pickupRequest'       : 'Pickup Request',
    'ws.smartBin'            : 'Smart Bin Monitor',
    'ws.collectionReport'    : 'Collection Report',
    // ── Payment ──
    'pay.online'             : 'Online Payment',
    'pay.history'            : 'Payment History',
    'pay.admin'              : 'Payment Admin',
    // ── Reports ──
    'rp.citizen'             : 'Citizen Report',
    'rp.service'             : 'Service Report',
    'rp.analytics'           : 'Analytics Report',
    // ── E-Voting ──
    'ev.voterReg'            : 'Voter Registration',
    'ev.candidate'           : 'Candidate List',
    // ── E-Tender ──
    'et.notices'             : 'E-Tender Notices',
    'et.bid'                 : 'E-Tender Bid',
    'et.admin'               : 'E-Tender Admin',
    'et.blacklist'           : 'Vendor Blacklist',
    // ── Communication ──
    'cm.sendNotif'           : 'Send Notification',
    'cm.notifLog'            : 'Notification Log',
    'cm.feedback'            : 'Citizen Feedback',
    'cm.feedbackAdmin'       : 'Feedback Admin',
    // ── Social Cards ──
    'sc.familyApply'         : 'Family Card (TCB) Apply',
    'sc.familyAdmin'         : 'Family Card Admin',
    'sc.familyStatus'        : 'Family Card Status',
    'sc.farmerApply'         : 'Farmer Card Apply',
    'sc.farmerAdmin'         : 'Farmer Card Admin',
    'sc.farmerStatus'        : 'Farmer Card Status',
    'sc.lpgApply'            : 'LPG Card Apply',
    'sc.lpgAdmin'            : 'LPG Card Admin',
    'sc.lpgStatus'           : 'LPG Card Status',
    'sc.vgdApply'            : 'VGD Card Apply',
    'sc.vgdAdmin'            : 'VGD Card Admin',
    'sc.vgdStatus'           : 'VGD Card Status',
    'sc.analytics'           : 'Social Cards Analytics',
    // ── Maps ──
    'mp.ward'                : 'Ward Map',
    'mp.holdings'            : 'Holdings Map',
    'mp.infrastructure'      : 'Infrastructure Map',
    // ── Notices ──
    'nt.admin'               : 'Notice Admin',
    'nt.public'              : 'Public Notice',
    // ── Admin ──
    'ad.profile'             : 'Profile',
    'ad.settings'            : 'Settings',
    'ad.systemSettings'      : 'System Settings',
    'ad.roles'               : 'User Roles',
    'ad.auditLogs'           : 'Audit Logs',
    'ad.userApproval'        : 'User Approval',
    // ── Farm Distribution ──
    'fd.tcbDistribution'     : 'TCB Distribution',
    'fd.vgdDistribution'     : 'VGD Distribution',
    'fd.farmerDistribution'  : 'Farmer Distribution',
    // ── Employee ──
    'em.registration'        : 'Employee Registration',
    'em.department'          : 'Department Assignment',
    'em.attendance'          : 'Attendance Management',
    'em.salary'              : 'Salary Management',
    'em.task'                : 'Task Assignment',
    // ── Footer ──
    'ft.rights'              : '© 2025 Municipal E-Governance. All rights reserved.',
    'ft.powered'             : 'Powered by Digital Bangladesh Initiative',
  },
  bn: {
    // ── Navbar ──
    'nav.eGovernance'        : 'ই-গভর্ন্যান্স',
    'nav.dashboard'          : 'ড্যাশবোর্ড',
    'nav.search'             : 'খুঁজুন...',
    'nav.searching'          : 'খোঁজা হচ্ছে...',
    'nav.nothingFound'       : 'এই অনুসন্ধানে কিছু পাওয়া যায়নি',
    'nav.notifications'      : 'বিজ্ঞপ্তি',
    'nav.noNotifications'    : 'কোনো বিজ্ঞপ্তি নেই',
    'nav.loading'            : 'লোড হচ্ছে...',
    'nav.seeAll'             : 'সব দেখুন →',
    'nav.profile'            : 'প্রোফাইল',
    'nav.settings'           : 'সেটিংস',
    'nav.logout'             : 'লগআউট',
    'nav.new'                : 'নতুন',
    // ── Sidebar sections ──
    'sb.services'            : 'সেবাসমূহ',
    'sb.dashboard'           : 'ড্যাশবোর্ড',
    'sb.citizenService'      : 'নাগরিক সেবা',
    'sb.tradeLicense'        : 'ট্রেড লাইসেন্স',
    'sb.holdingTax'          : 'হোল্ডিং ট্যাক্স',
    'sb.complaint'           : 'অভিযোগ',
    'sb.infrastructure'      : 'অবকাঠামো',
    'sb.healthSanitation'    : 'স্বাস্থ্য ও স্যানিটেশন',
    'sb.project'             : 'প্রকল্প',
    'sb.waterSupply'         : 'পানি সরবরাহ',
    'sb.wasteManagement'     : 'বর্জ্য ব্যবস্থাপনা',
    'sb.payment'             : 'পেমেন্ট',
    'sb.reports'             : 'প্রতিবেদন',
    'sb.election'            : 'ই-ভোটিং',
    'sb.etender'             : 'ই-টেন্ডার',
    'sb.communication'       : 'যোগাযোগ',
    'sb.socialCards'         : 'সামাজিক কার্ড',
    'sb.maps'                : 'মানচিত্র',
    'sb.notices'             : 'নোটিশ',
    'sb.admin'               : 'প্রশাসন',
    'sb.farmDistribution'    : 'কৃষি বিতরণ',
    'sb.employee'            : 'কর্মচারী',
    // ── Citizen Service ──
    'cs.birthDeath'          : 'জন্ম / মৃত্যু সনদ',
    'cs.citizenCert'         : 'নাগরিক সনদ',
    'cs.familyCert'          : 'পারিবারিক সনদ',
    'cs.passport'            : 'পাসপোর্ট',
    'cs.appStatus'           : 'আবেদনের অবস্থা',
    'cs.passportAdmin'       : 'পাসপোর্ট অ্যাডমিন',
    'cs.allApplyData'        : 'সব আবেদন তথ্য',
    // ── Trade License ──
    'tl.applyLicense'        : 'লাইসেন্সের আবেদন',
    'tl.renewal'             : 'লাইসেন্স নবায়ন',
    'tl.statusCheck'         : 'অবস্থা যাচাই',
    'tl.allApplyData'        : 'সব আবেদন তথ্য',
    // ── Holding Tax ──
    'ht.newRegistration'     : 'নতুন নিবন্ধন',
    'ht.taxAssessment'       : 'কর নির্ধারণ',
    'ht.taxPayment'          : 'কর পরিশোধ',
    'ht.taxDueList'          : 'বকেয়া কর তালিকা',
    'ht.ownershipTransfer'   : 'মালিকানা হস্তান্তর',
    'ht.collectionReport'    : 'আদায় প্রতিবেদন',
    'ht.taxApplications'     : 'কর আবেদন',
    // ── Complaint ──
    'cp.submit'              : 'অভিযোগ দাখিল',
    'cp.allComplaints'       : 'সব অভিযোগ',
    'cp.tracking'            : 'ট্র্যাকিং',
    'cp.resolution'          : 'নিষ্পত্তি',
    // ── Infrastructure ──
    'inf.road'               : 'রাস্তা রক্ষণাবেক্ষণ',
    'inf.drainage'           : 'ড্রেনেজ',
    'inf.streetLight'        : 'সড়কবাতি',
    'inf.construction'       : 'নির্মাণ অনুমতি',
    'inf.status'             : 'অবকাঠামোর অবস্থা',
    // ── Health ──
    'hl.notices'             : 'স্বাস্থ্য বিজ্ঞপ্তি',
    'hl.epiRegister'         : 'ইপিআই নিবন্ধন',
    'hl.epiAdmin'            : 'ইপিআই অ্যাডমিন',
    'hl.sanitation'          : 'পয়ঃনিষ্কাশন পর্যবেক্ষণ',
    'hl.centerInfo'          : 'স্বাস্থ্য কেন্দ্রের তথ্য',
    // ── Project ──
    'pj.list'                : 'প্রকল্প তালিকা',
    'pj.budget'              : 'প্রকল্প বাজেট',
    // ── Water ──
    'wt.connection'          : 'পানি সংযোগ',
    'wt.bill'                : 'পানির বিল',
    'wt.usageReport'         : 'পানি ব্যবহার প্রতিবেদন',
    'wt.wardList'            : 'ওয়ার্ড তালিকা',
    'wt.population'          : 'জনসংখ্যা',
    // ── Waste ──
    'ws.schedule'            : 'বর্জ্য সময়সূচি',
    'ws.pickupRequest'       : 'পিকআপ অনুরোধ',
    'ws.smartBin'            : 'স্মার্ট বিন মনিটর',
    'ws.collectionReport'    : 'সংগ্রহ প্রতিবেদন',
    // ── Payment ──
    'pay.online'             : 'অনলাইন পেমেন্ট',
    'pay.history'            : 'পেমেন্ট ইতিহাস',
    'pay.admin'              : 'পেমেন্ট অ্যাডমিন',
    // ── Reports ──
    'rp.citizen'             : 'নাগরিক প্রতিবেদন',
    'rp.service'             : 'সেবা প্রতিবেদন',
    'rp.analytics'           : 'বিশ্লেষণ প্রতিবেদন',
    // ── E-Voting ──
    'ev.voterReg'            : 'ভোটার নিবন্ধন',
    'ev.candidate'           : 'প্রার্থী তালিকা',
    // ── E-Tender ──
    'et.notices'             : 'ই-টেন্ডার বিজ্ঞপ্তি',
    'et.bid'                 : 'ই-টেন্ডার বিড',
    'et.admin'               : 'ই-টেন্ডার অ্যাডমিন',
    'et.blacklist'           : 'ব্ল্যাকলিস্টেড বিক্রেতা',
    // ── Communication ──
    'cm.sendNotif'           : 'বিজ্ঞপ্তি পাঠান',
    'cm.notifLog'            : 'বিজ্ঞপ্তি লগ',
    'cm.feedback'            : 'নাগরিক মতামত',
    'cm.feedbackAdmin'       : 'মতামত অ্যাডমিন',
    // ── Social Cards ──
    'sc.familyApply'         : 'পারিবারিক কার্ড (টিসিবি) আবেদন',
    'sc.familyAdmin'         : 'পারিবারিক কার্ড অ্যাডমিন',
    'sc.familyStatus'        : 'পারিবারিক কার্ডের অবস্থা',
    'sc.farmerApply'         : 'কৃষক কার্ড আবেদন',
    'sc.farmerAdmin'         : 'কৃষক কার্ড অ্যাডমিন',
    'sc.farmerStatus'        : 'কৃষক কার্ডের অবস্থা',
    'sc.lpgApply'            : 'এলপিজি কার্ড আবেদন',
    'sc.lpgAdmin'            : 'এলপিজি কার্ড অ্যাডমিন',
    'sc.lpgStatus'           : 'এলপিজি কার্ডের অবস্থা',
    'sc.vgdApply'            : 'ভিজিডি কার্ড আবেদন',
    'sc.vgdAdmin'            : 'ভিজিডি কার্ড অ্যাডমিন',
    'sc.vgdStatus'           : 'ভিজিডি কার্ডের অবস্থা',
    'sc.analytics'           : 'সামাজিক কার্ড বিশ্লেষণ',
    // ── Maps ──
    'mp.ward'                : 'ওয়ার্ড মানচিত্র',
    'mp.holdings'            : 'হোল্ডিং মানচিত্র',
    'mp.infrastructure'      : 'অবকাঠামো মানচিত্র',
    // ── Notices ──
    'nt.admin'               : 'নোটিশ অ্যাডমিন',
    'nt.public'              : 'পাবলিক নোটিশ',
    // ── Admin ──
    'ad.profile'             : 'প্রোফাইল',
    'ad.settings'            : 'সেটিংস',
    'ad.systemSettings'      : 'সিস্টেম সেটিংস',
    'ad.roles'               : 'ব্যবহারকারীর ভূমিকা',
    'ad.auditLogs'           : 'অডিট লগ',
    'ad.userApproval'        : 'ব্যবহারকারী অনুমোদন',
    // ── Farm Distribution ──
    'fd.tcbDistribution'     : 'টিসিবি বিতরণ',
    'fd.vgdDistribution'     : 'ভিজিডি বিতরণ',
    'fd.farmerDistribution'  : 'কৃষক বিতরণ',
    // ── Employee ──
    'em.registration'        : 'কর্মচারী নিবন্ধন',
    'em.department'          : 'বিভাগ নিয়োগ',
    'em.attendance'          : 'উপস্থিতি ব্যবস্থাপনা',
    'em.salary'              : 'বেতন ব্যবস্থাপনা',
    'em.task'                : 'কাজ বরাদ্দ',
    // ── Footer ──
    'ft.rights'              : '© ২০২৫ মিউনিসিপ্যাল ই-গভর্ন্যান্স। সর্বস্বত্ব সংরক্ষিত।',
    'ft.powered'             : 'ডিজিটাল বাংলাদেশ উদ্যোগ দ্বারা পরিচালিত',
  }
};
