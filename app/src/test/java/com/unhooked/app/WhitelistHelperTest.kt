package com.unhooked.app

import com.unhooked.app.domain.whitelist.WhitelistHelper
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WhitelistHelperTest {

    @Test
    fun paytmAndDigiLockerAreAlwaysWhitelisted() {
        // Paytm
        assertTrue("Paytm must be whitelisted", WhitelistHelper.isCriticalApp("net.one97.paytm"))
        assertTrue("Paytm Business must be whitelisted", WhitelistHelper.isCriticalApp("com.paytm.business"))

        // DigiLocker & Gov
        assertTrue("DigiLocker must be whitelisted", WhitelistHelper.isCriticalApp("in.gov.digilocker.app"))
        assertTrue("mAadhaar must be whitelisted", WhitelistHelper.isCriticalApp("in.gov.uidai.mAadhaarPlus"))
        assertTrue("UMANG must be whitelisted", WhitelistHelper.isCriticalApp("in.gov.umang.negd.g2c"))

        // UPI & Banking
        assertTrue("PhonePe must be whitelisted", WhitelistHelper.isCriticalApp("com.phonepe.app"))
        assertTrue("Google Pay must be whitelisted", WhitelistHelper.isCriticalApp("com.google.android.apps.nbu.paisa.user"))
        assertTrue("BHIM must be whitelisted", WhitelistHelper.isCriticalApp("in.org.npci.upiapp"))
        assertTrue("YONO SBI must be whitelisted", WhitelistHelper.isCriticalApp("com.sbi.lotusintouch"))
        assertTrue("HDFC must be whitelisted", WhitelistHelper.isCriticalApp("com.snapwork.hdfc"))

        // System Core & Unhooked
        assertTrue("Unhooked itself must be whitelisted", WhitelistHelper.isCriticalApp("com.unhooked.app"))
        assertTrue("System UI must be whitelisted", WhitelistHelper.isCriticalApp("com.android.systemui"))
        assertTrue("Settings must be whitelisted", WhitelistHelper.isCriticalApp("com.android.settings"))
        assertTrue("Phone dialer must be whitelisted", WhitelistHelper.isCriticalApp("com.android.dialer"))
    }

    @Test
    fun distractingAppsAreEligibleForBlocking() {
        assertFalse("Instagram is not critical", WhitelistHelper.isCriticalApp("com.instagram.android"))
        assertFalse("TikTok is not critical", WhitelistHelper.isCriticalApp("com.zhiliaoapp.musically"))
        assertFalse("YouTube is not critical", WhitelistHelper.isCriticalApp("com.google.android.youtube"))
        assertFalse("X/Twitter is not critical", WhitelistHelper.isCriticalApp("com.twitter.android"))

        assertTrue("Instagram is eligible for blocking", WhitelistHelper.isEligibleForBlocking("com.instagram.android"))
        assertFalse("Paytm is NOT eligible for blocking", WhitelistHelper.isEligibleForBlocking("net.one97.paytm"))
        assertFalse("DigiLocker is NOT eligible for blocking", WhitelistHelper.isEligibleForBlocking("in.gov.digilocker.app"))
    }
}
