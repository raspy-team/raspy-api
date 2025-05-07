package com.raspy.backend.s3

import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import java.nio.file.Paths
import java.util.*
@Component
class S3Uploader(
    @Value("\${cloud.aws.s3.bucket}") private val bucket: String,
    @Value("\${cloud.aws.region.static}") private val region: String,
    @Value("\${cloud.aws.credentials.access-key}") private val accessKey: String,
    @Value("\${cloud.aws.credentials.secret-key}") private val secretKey: String,
) {
    private val s3Client: S3Client = S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
        )
        .build()

    fun upload(file: MultipartFile): String {
        val fileName = "profile/${UUID.randomUUID()}-${file.originalFilename}"
        val tempFile = kotlin.io.path.createTempFile().toFile().apply {
            file.transferTo(this)
        }

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(fileName)
            .contentType(file.contentType)
            //  .acl("public-read")
            .build()

        return try {
            s3Client.putObject(putObjectRequest, Paths.get(tempFile.toURI()))
            "https://d1iimlpplvq3em.cloudfront.net/$fileName"
        } catch (e: S3Exception) {
            throw RuntimeException("S3 업로드 실패 (region 또는 key 값이 옳은지 점검해보세요): ${e.awsErrorDetails().errorMessage()}")
        } finally {
            /**
             * s3에 올리면 로컬에 임시 저장한 파일은 지워도 됨
             */
            tempFile.delete()
        }

    }
}
