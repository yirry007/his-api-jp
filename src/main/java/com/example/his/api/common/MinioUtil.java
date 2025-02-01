package com.example.his.api.common;

import cn.hutool.core.codec.Base64;
import com.example.his.api.exception.HisException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Component
@Slf4j
public class MinioUtil {
    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    private String bucket;

    private MinioClient client;

    @PostConstruct
    public void init() {
        this.client = new MinioClient.Builder().endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    public void uploadImage(String path, MultipartFile file) {
        try {
            // Save image in minio（less than 5M）
            this.client.putObject(PutObjectArgs.builder()
                    .bucket(bucket).object(path)
                    .stream(file.getInputStream(), -1, 5 * 1024 * 1024)
                    .contentType("image/jpeg").build());
            log.debug(path + "ファイルの保存に成功しました。");
        } catch (Exception e) {
            log.error("ファイルの保存に失敗しました。", e);
            throw new HisException("ファイルの保存に失敗しました。");
        }
    }

    public void uploadExcel(String path, MultipartFile file) {
        try {
            //MIME type of excel
            String mime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            //Less than 20M
            this.client.putObject(PutObjectArgs.builder()
                    .bucket(bucket).object(path)
                    .stream(file.getInputStream(), -1, 20 * 1024 * 1024)
                    .contentType(mime).build());
            log.debug(path + "ファイルの保存に成功しました。");
        } catch (Exception e) {
            log.error("ファイルの保存に失敗しました。", e);
            throw new HisException("ファイルの保存に失敗しました。");
        }
    }

    public InputStream downloadFile(String path) {
        try {
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .build();
            return client.getObject(args);
        } catch (Exception e) {
            log.error("ファイルのダウンロードに失敗しました。", e);
            throw new HisException("ファイルのダウンロードに失敗しました。");
        }
    }

    public void deleteFile(String path) {
        try {
            this.client.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(path)
                    .build());
            log.debug(path + "のパスにあるファイルを削除しました。");
        } catch (Exception e) {
            log.error("ファイルを削除に失敗しました。", e);
            throw new HisException("ファイルを削除に失敗しました。");
        }
    }

    public void uploadImage(String path, String base64Image) {
        try {
            // Delete base64 prefix
            base64Image = base64Image.replace("data:image/jpeg;base64,", "");
            base64Image = base64Image.replace("data:image/png;base64,", "");
            byte[] decode = Base64.decode(base64Image);
            ByteArrayInputStream in = new ByteArrayInputStream(decode);
            // Save image（less than 5M）
            this.client.putObject(PutObjectArgs.builder().bucket(bucket).object(path).stream(in, -1, 5 * 1024 * 1024).contentType("image/jpeg").build());
            log.debug(path + "ファイルの保存に成功しました。");
        } catch (Exception e) {
            log.error("ファイルの保存に失敗しました。", e);
            throw new HisException("ファイルの保存に失敗しました。");
        }
    }

    public void uploadWord(String path, InputStream in) {
        try {
            String mime = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            // Save word file（less than 50M）
            this.client.putObject(PutObjectArgs.builder().bucket(bucket)
                    .object(path).stream(in, -1, 50 * 1024 * 1024)
                    .contentType(mime).build());
            log.debug(path + "ファイルの保存に成功しました。");
            in.close();
        } catch (Exception e) {
            log.error("ファイルの保存に失敗しました。", e);
            throw new HisException("ファイルの保存に失敗しました。");
        }
    }
}

