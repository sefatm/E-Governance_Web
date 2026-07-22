package com.mgt.model;

/**
 * BusinessCategory — অনুমোদিত Business Category List
 *
 * প্রতিটা category তে:
 *   - nameEn     → English নাম
 *   - nameBn     → বাংলা নাম
 *   - baseFee    → বার্ষিক minimum license fee (টাকা)
 *   - requiresInspection → Approve করার আগে physical inspection লাগবে কিনা
 *
 * Usage:
 *   BusinessCategory.getAll()          → Angular dropdown এর জন্য সব categories
 *   BusinessCategory.isValid("Retail") → Validation এর জন্য
 *   BusinessCategory.getByName("Retail").getBaseFee() → Fee calculation
 */
public enum BusinessCategory {

    // ── খাদ্য ও পানীয় ────────────────────────────────────────────────────────
    RESTAURANT       ("Restaurant",        "রেস্তোরাঁ / হোটেল",          5000.0,  true),
    FOOD_STALL       ("Food Stall",        "খাবারের দোকান / ফুডস্টল",    2000.0,  true),
    BAKERY           ("Bakery",            "বেকারি",                      3000.0,  true),
    GROCERY          ("Grocery",           "মুদি দোকান / মুদিখানা",       2000.0,  false),
    SWEETS_SHOP      ("Sweets Shop",       "মিষ্টির দোকান",               3000.0,  true),

    // ── কাপড় ও পোশাক ─────────────────────────────────────────────────────────
    CLOTHING         ("Clothing",          "কাপড়ের দোকান / বস্ত্র",      3000.0,  false),
    TAILORING        ("Tailoring",         "টেইলারিং / দর্জি দোকান",     1500.0,  false),
    FOOTWEAR         ("Footwear",          "জুতার দোকান",                 2000.0,  false),

    // ── ইলেক্ট্রনিক্স ও প্রযুক্তি ───────────────────────────────────────────
    ELECTRONICS      ("Electronics",       "ইলেক্ট্রনিক্স / আড়ত",        4000.0,  false),
    MOBILE_SHOP      ("Mobile Shop",       "মোবাইল ফোনের দোকান",          3000.0,  false),
    COMPUTER_SHOP    ("Computer Shop",     "কম্পিউটার / ল্যাপটপ দোকান",   4000.0,  false),
    IT_SERVICE       ("IT Service",        "IT সেবা / Software House",     5000.0,  false),

    // ── স্বাস্থ্য ও চিকিৎসা ──────────────────────────────────────────────────
    PHARMACY         ("Pharmacy",          "ওষুধের দোকান / ফার্মেসি",     5000.0,  true),
    CLINIC           ("Clinic",            "ক্লিনিক / চেম্বার",           8000.0,  true),
    DIAGNOSTIC       ("Diagnostic",        "ডায়াগনস্টিক সেন্টার",         10000.0, true),
    OPTICAL_SHOP     ("Optical Shop",      "চশমার দোকান / অপটিক্যাল",     3000.0,  false),

    // ── নির্মাণ ও হার্ডওয়্যার ────────────────────────────────────────────────
    HARDWARE         ("Hardware",          "হার্ডওয়্যার / নির্মাণ সামগ্রী", 4000.0, false),
    CONSTRUCTION     ("Construction",      "কনস্ট্রাকশন / ঠিকাদারি",     10000.0, true),
    PAINT_SHOP       ("Paint Shop",        "রঙের দোকান",                  3000.0,  false),
    FURNITURE        ("Furniture",         "আসবাবপত্রের দোকান",            4000.0,  false),

    // ── গাড়ি ও যানবাহন ───────────────────────────────────────────────────────
    CAR_WORKSHOP     ("Car Workshop",      "গাড়ি মেরামত / গ্যারেজ",       5000.0,  true),
    CAR_PARTS        ("Car Parts",         "গাড়ির যন্ত্রাংশ",             4000.0,  false),
    FUEL_STATION     ("Fuel Station",      "পেট্রোল / ফুয়েল স্টেশন",     15000.0, true),
    TRANSPORT        ("Transport",         "পরিবহন সেবা",                 8000.0,  true),

    // ── শিক্ষা ও প্রশিক্ষণ ───────────────────────────────────────────────────
    COACHING         ("Coaching Center",   "কোচিং সেন্টার",               4000.0,  true),
    SCHOOL           ("School",            "স্কুল / শিক্ষা প্রতিষ্ঠান",   8000.0,  true),
    LIBRARY          ("Library",           "লাইব্রেরি / পাঠাগার",         2000.0,  false),

    // ── ব্যাংকিং ও আর্থিক সেবা ───────────────────────────────────────────────
    BANK_AGENT       ("Bank Agent",        "ব্যাংক এজেন্ট / মোবাইল ব্যাংকিং", 5000.0, true),
    INSURANCE        ("Insurance",         "বীমা / ইন্স্যুরেন্স",         6000.0,  true),
    MONEY_EXCHANGE   ("Money Exchange",    "মানি এক্সচেঞ্জ",              8000.0,  true),

    // ── হোটেল ও পর্যটন ───────────────────────────────────────────────────────
    HOTEL            ("Hotel",             "হোটেল / আবাসিক",             10000.0, true),
    TRAVEL_AGENCY    ("Travel Agency",     "ট্রাভেল এজেন্সি",             5000.0,  false),

    // ── কৃষি ও পশুপালন ───────────────────────────────────────────────────────
    AGRO_FARM        ("Agro Farm",         "কৃষি / খামার",                3000.0,  true),
    FISH_MARKET      ("Fish Market",       "মাছের বাজার / আড়ত",           3000.0,  true),
    POULTRY          ("Poultry",           "পোল্ট্রি / মুরগির খামার",      4000.0,  true),

    // ── সেবা ─────────────────────────────────────────────────────────────────
    SALON            ("Salon / Beauty",    "সেলুন / বিউটি পার্লার",       2000.0,  false),
    LAUNDRY          ("Laundry",           "লন্ড্রি / ড্রাই ক্লিনিং",     2000.0,  false),
    PRINTING         ("Printing",          "প্রিন্টিং / ফটোকপি",          3000.0,  false),
    GAS_DEALER       ("Gas Dealer",        "গ্যাস সিলিন্ডার ডিলার",       6000.0,  true),
    COLD_STORAGE     ("Cold Storage",      "কোল্ড স্টোরেজ",              12000.0,  true),

    // ── সাধারণ ব্যবসা ─────────────────────────────────────────────────────────
    RETAIL           ("Retail",            "খুচরা ব্যবসা (সাধারণ)",       2000.0,  false),
    WHOLESALE        ("Wholesale",         "পাইকারি ব্যবসা",              6000.0,  false),
    IMPORT_EXPORT    ("Import/Export",     "আমদানি/রপ্তানি",             15000.0, true),
    OTHER            ("Other",             "অন্যান্য ব্যবসা",              2000.0,  false);

    // ─────────────────────────────────────────────────────────────────────────

    private final String  nameEn;
    private final String  nameBn;
    private final double  baseFee;
    private final boolean requiresInspection;

    BusinessCategory(String nameEn, String nameBn, double baseFee, boolean requiresInspection) {
        this.nameEn             = nameEn;
        this.nameBn             = nameBn;
        this.baseFee            = baseFee;
        this.requiresInspection = requiresInspection;
    }

    public String  getNameEn()             { return nameEn; }
    public String  getNameBn()             { return nameBn; }
    public double  getBaseFee()            { return baseFee; }
    public boolean isRequiresInspection()  { return requiresInspection; }

    /** Angular dropdown এর জন্য সব categories DTO হিসেবে return করো */
    public static java.util.List<java.util.Map<String, Object>> getAll() {
        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        for (BusinessCategory c : values()) {
            java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("key",                c.name());
            map.put("nameEn",             c.nameEn);
            map.put("nameBn",             c.nameBn);
            map.put("baseFee",            c.baseFee);
            map.put("requiresInspection", c.requiresInspection);
            list.add(map);
        }
        return list;
    }

    /**
     * Validation — submitted businessType টা approved list এ আছে কিনা
     * Case-insensitive match করে nameEn এর সাথে
     */
    public static boolean isValid(String businessType) {
        if (businessType == null || businessType.isBlank()) return false;
        for (BusinessCategory c : values()) {
            if (c.nameEn.equalsIgnoreCase(businessType.trim())) return true;
        }
        return false;
    }

    /**
     * nameEn দিয়ে category খুঁজো
     * @return null যদি না পাওয়া যায়
     */
    public static BusinessCategory getByName(String nameEn) {
        if (nameEn == null) return null;
        for (BusinessCategory c : values()) {
            if (c.nameEn.equalsIgnoreCase(nameEn.trim())) return c;
        }
        return null;
    }
}
