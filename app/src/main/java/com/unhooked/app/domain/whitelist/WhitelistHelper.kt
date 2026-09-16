package com.unhooked.app.domain.whitelist

/**
 * WhitelistHelper provides an immutable safeguard for critical financial, government,
 * emergency, and system-level applications.
 *
 * Ensures Unhooked never intercepts, blocks, or interferes with sensitive applications
 * such as Paytm, DigiLocker, PhonePe, Google Pay, and official banking suites.
 */
object WhitelistHelper {

    val UNHOOKED_PACKAGE = "com.unhooked.app"

    private val FINANCIAL_AND_PAYMENT_PACKAGES = setOf(
        "net.one97.paytm",                   // Paytm
        "com.paytm.business",                // Paytm for Business
        "com.phonepe.app",                   // PhonePe
        "com.phonepe.app.business",          // PhonePe Business
        "com.google.android.apps.nbu.paisa.user", // Google Pay (Tez)
        "in.org.npci.upiapp",                // BHIM UPI
        "com.dreamplug.androidapp",          // CRED
        "com.navi.loan",                     // Navi
        "com.mobikwik_new",                  // MobiKwik
        "com.sbi.lotusintouch",              // YONO SBI
        "com.sbi.SBIFreedomPlus",            // SBI Anywhere
        "com.snapwork.hdfc",                 // HDFC Bank MobileBanking
        "com.csam.icici.bank.imobile",       // iMobile Pay by ICICI Bank
        "com.axis.mobile",                   // Axis Mobile
        "com.msf.kbank.mobile",              // Kotak - 811 & Mobile Banking
        "com.bob.bobworld",                  // bob World (Bank of Baroda)
        "com.pnb.one",                       // PNB ONE
        "com.canarabank.mobility"            // Canara ai1 Mobile Banking
    )

    private val GOVERNMENT_AND_IDENTITY_PACKAGES = setOf(
        "in.gov.digilocker.app",             // DigiLocker
        "in.gov.uidai.mAadhaarPlus",         // mAadhaar
        "in.gov.umang.negd.g2c",             // UMANG (Government of India)
        "gov.mparivahan"                     // NextGen mParivahan
    )

    private val ESSENTIAL_SYSTEM_PACKAGES = setOf(
        UNHOOKED_PACKAGE,                    // Unhooked itself
        "com.android.systemui",              // Android System UI
        "com.android.settings",              // Android Settings
        "com.android.dialer",                // Android Phone
        "com.google.android.dialer",         // Google Phone
        "com.samsung.android.dialer",        // Samsung Phone
        "com.android.server.telecom",        // Android Telecom
        "com.android.emergency",             // Emergency SOS
        "com.android.contacts",              // Contacts
        "com.google.android.contacts",       // Google Contacts
        "com.android.permissioncontroller",  // Android Permission Controller
        "com.google.android.packageinstaller", // Google Package Installer
        "com.android.packageinstaller",      // Android Package Installer
        "com.android.vending",               // Google Play Store
        "com.google.android.gms",            // Google Play Services
        "com.google.android.apps.nexuslauncher", // Pixel Launcher
        "com.sec.android.app.launcher"       // Samsung One UI Home
    )

    /**
     * Checks if the given package is permanently whitelisted.
     * Guaranteed constant-time O(1) set lookup.
     */
    fun isCriticalApp(packageName: String): Boolean {
        if (packageName.isBlank()) return true
        if (packageName == UNHOOKED_PACKAGE) return true
        return FINANCIAL_AND_PAYMENT_PACKAGES.contains(packageName) ||
                GOVERNMENT_AND_IDENTITY_PACKAGES.contains(packageName) ||
                ESSENTIAL_SYSTEM_PACKAGES.contains(packageName)
    }

    /**
     * Checks whether a package is eligible for blocking.
     * Returns true ONLY if the package is NOT in the critical whitelist.
     */
    fun isEligibleForBlocking(packageName: String): Boolean {
        return !isCriticalApp(packageName)
    }
}
