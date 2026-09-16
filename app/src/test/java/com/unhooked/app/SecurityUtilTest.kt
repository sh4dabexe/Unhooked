package com.unhooked.app

import com.unhooked.app.domain.security.SecurityUtil
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityUtilTest {

    @Test
    fun saltedHashVerifiesMatchingPassword() {
        val password = "StrongFocusPassword#123"
        val salt = SecurityUtil.generateSalt()

        val hash = SecurityUtil.hashPassword(password, salt)
        assertTrue("Hash should not be blank", hash.isNotBlank())

        val isValid = SecurityUtil.verifyPassword(password, hash, salt)
        assertTrue("Password verification should succeed for valid password", isValid)
    }

    @Test
    fun wrongPasswordFailsVerification() {
        val password = "CorrectPassword"
        val salt = SecurityUtil.generateSalt()
        val hash = SecurityUtil.hashPassword(password, salt)

        val isValid = SecurityUtil.verifyPassword("WrongPassword", hash, salt)
        assertFalse("Verification must fail for wrong password", isValid)
    }

    @Test
    fun differentSaltsProduceDifferentHashes() {
        val password = "SamePassword"
        val salt1 = SecurityUtil.generateSalt()
        val salt2 = SecurityUtil.generateSalt()

        val hash1 = SecurityUtil.hashPassword(password, salt1)
        val hash2 = SecurityUtil.hashPassword(password, salt2)

        assertNotEquals("Hashes with different salts must not be identical", hash1, hash2)
    }
}
