package com.exampledemo.parsaniahardik.registerloginsession

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object ChCrypto{
    @JvmStatic fun aesEncrypt(v:String, secretKey: ByteArray, iv: ByteArray) = AES256.encrypt(v, secretKey, iv)
    @JvmStatic fun aesDecrypt(v:String, secretKey: ByteArray, iv: ByteArray) = AES256.decrypt(v, secretKey, iv)
}

private object AES256{
    private val encorder = Base64.getEncoder()
    private val decorder = Base64.getDecoder()
    private fun cipher(opmode:Int, secretKey: ByteArray, iv: ByteArray):Cipher{
        if(secretKey.length != 32) throw RuntimeException("SecretKey length is not 32 chars")
        val c = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val sk = SecretKeySpec(secretKey, "AES")
//        val iv = IvParameterSpec(secretKey.substring(0, 16).toByteArray(Charsets.UTF_8))
        c.init(opmode, sk, iv)
        return c
    }
    fun encrypt(str:String, secretKey: ByteArray, iv: ByteArray):String{
        val encrypted = cipher(Cipher.ENCRYPT_MODE, secretKey, iv).doFinal(str.toByteArray(Charsets.UTF_8))
        return String(encorder.encode(encrypted))
    }
    fun decrypt(str:String, secretKey: ByteArray, iv: ByteArray):String{
        val byteStr = decorder.decode(str.toByteArray(Charsets.UTF_8))
        return String(cipher(Cipher.DECRYPT_MODE, secretKey, iv).doFinal(byteStr))
    }
}