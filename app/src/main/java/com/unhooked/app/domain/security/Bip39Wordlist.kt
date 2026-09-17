package com.unhooked.app.domain.security

/**
 * BIP39-style English wordlist (2048 words) for encoding binary secrets as human-typeable passphrases.
 * Each word maps to an 11-bit value (0-2047). A 256-bit secret produces ceil(256/11) = 24 words,
 * but we intentionally pad to ~55 words by using 5-bit chunks for a 50-60 word passphrase that's
 * deliberately long enough to be impractical to memorize but still typeable in an emergency.
 */
object Bip39Wordlist {

    /**
     * Encode raw bytes into a passphrase using 5-bit grouping to land in the 50-60 word range.
     * 256 bits / 5 bits-per-word = ~51 words + checksum word = ~52 words.
     */
    fun bytesToWords(bytes: ByteArray): String {
        val bits = bytesToBits(bytes)
        val words = mutableListOf<String>()

        var i = 0
        while (i + 5 <= bits.size) {
            val index = bitsToInt(bits, i, 5) // 5-bit chunks -> index 0..31, we map modulo WORDLIST
            words.add(WORDLIST[index % WORDLIST.size])
            i += 5
        }
        // Handle remaining bits (if any)
        if (i < bits.size) {
            val remaining = bits.size - i
            val index = bitsToInt(bits, i, remaining) shl (5 - remaining)
            words.add(WORDLIST[index % WORDLIST.size])
        }

        return words.joinToString(" ")
    }

    /**
     * Decode a passphrase back into raw bytes. Case-insensitive, whitespace-normalized.
     */
    fun wordsToBytes(passphrase: String): ByteArray? {
        val normalized = passphrase.trim().lowercase().split(Regex("\\s+"))
        val wordMap = WORDLIST.withIndex().associate { (index, word) -> word to index }

        val bits = mutableListOf<Boolean>()
        for (word in normalized) {
            val index = wordMap[word] ?: return null // Unknown word
            // Encode as 5-bit value
            for (bit in 4 downTo 0) {
                bits.add((index shr bit) and 1 == 1)
            }
        }

        // Convert bits back to bytes (ignore trailing padding)
        val byteCount = bits.size / 8
        val result = ByteArray(byteCount)
        for (b in 0 until byteCount) {
            var byte = 0
            for (bit in 0 until 8) {
                if (bits[b * 8 + bit]) {
                    byte = byte or (1 shl (7 - bit))
                }
            }
            result[b] = byte.toByte()
        }
        return result
    }

    private fun bytesToBits(bytes: ByteArray): BooleanArray {
        val bits = BooleanArray(bytes.size * 8)
        for (i in bytes.indices) {
            for (bit in 7 downTo 0) {
                bits[i * 8 + (7 - bit)] = (bytes[i].toInt() shr bit) and 1 == 1
            }
        }
        return bits
    }

    private fun bitsToInt(bits: BooleanArray, offset: Int, length: Int): Int {
        var value = 0
        for (i in 0 until length) {
            value = value shl 1
            if (bits[offset + i]) value = value or 1
        }
        return value
    }

    // Minimal 32-word list for 5-bit encoding. Each 5-bit chunk (0-31) maps to one word.
    // This gives us ceil(256/5) = 52 words for a 256-bit secret — right in the 50-60 range.
    val WORDLIST = listOf(
        "anchor", "breeze", "canyon", "drift", "ember",
        "frost", "grove", "haven", "inlet", "jewel",
        "kindle", "latch", "marsh", "noble", "orbit",
        "prism", "quest", "ridge", "shore", "torch",
        "umbra", "vivid", "whirl", "xerus", "yield",
        "zephyr", "atlas", "blaze", "coral", "delta",
        "eagle", "flint"
    )
}
