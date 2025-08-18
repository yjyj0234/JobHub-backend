// package boot.data.jobhub;

// // S3Config.java (추가)
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
// import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
// import software.amazon.awssdk.regions.Region;
// import software.amazon.awssdk.services.s3.S3Client;
// import software.amazon.awssdk.services.s3.presigner.S3Presigner;

// @Configuration
// public class S3Config {

//     @Value("${cloud.aws.region}") String region;
//     @Value("${cloud.aws.credentials.accessKey}") String accessKey;
//     @Value("${cloud.aws.credentials.secretKey}") String secretKey;

// // ✅ S3Client 빈
//     @Bean
//     public S3Client s3Client() {
//         return S3Client.builder()
//                 .region(Region.of(region))
//                 .credentialsProvider(
//                         StaticCredentialsProvider.create(
//                                 AwsBasicCredentials.create(accessKey.trim(), secretKey.trim())
//                         )
//                 )
//                 .build();
//     }

//     // 이미 S3Client 빈이 있다면 그대로 두고, Presigner만 추가
//     @Bean
//     public S3Presigner s3Presigner() {
//         return S3Presigner.builder()
//                 .region(Region.of(region))
//                 .credentialsProvider(
//                         StaticCredentialsProvider.create(
//                                 AwsBasicCredentials.create(accessKey.trim(), secretKey.trim())
//                         )
//                 )
//                 .build();
//     }
// }
