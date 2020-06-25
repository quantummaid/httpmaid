/*
 * Copyright (c) 2020 Richard Hauswald - https://quantummaid.de/.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.quantummaid.httpmaid.remotespecs.lambda.aws.s3;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;

@Slf4j
public final class S3Handler {

    private S3Handler() {
    }

    public static String uploadToS3Bucket(final String bucketName,
                                          final File file) {
        final String key = keyFromFile(file);
        try (S3Client s3Client = S3Client.create()) {
            log.info("Uploading {} to S3 object {}/{}...", file, bucketName, key);
            if (!fileNeedsUploading(bucketName, key, s3Client)) {
                log.info("S3 object with matching MD5 already present, skipping upload.");
            } else {
                log.info("S3 object not already present, uploading...");
                s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(), file.toPath());
                log.info("Uploaded {} to S3 object {}/{}.", file, bucketName, key);
            }
            return key;
        }
    }

    private static boolean fileNeedsUploading(final String bucketName,
                                              final String key,
                                              final S3Client s3Client) {
        final ListObjectsResponse objectsResponse = s3Client.listObjects(ListObjectsRequest.builder()
                .bucket(bucketName)
                .build());
        return objectsResponse.contents().stream()
                .map(S3Object::key)
                .noneMatch(key::equals);
    }

    private static String keyFromFile(final File file) {
        final Md5Checksum newContentMD5 = Md5Checksum.ofFile(file);
        return newContentMD5.getValue();
    }

    public static void deleteAllObjectsInBucket(final String bucketName) {
        try (S3Client s3Client = S3Client.create()) {
            final ListObjectsResponse listObjectsResponse = s3Client.listObjects(ListObjectsRequest.builder()
                    .bucket(bucketName)
                    .build());
            listObjectsResponse.contents().forEach(s3Object -> {
                final String key = s3Object.key();
                deleteFromS3Bucket(bucketName, key, s3Client);
            });
        }
    }

    private static void deleteFromS3Bucket(final String bucketName,
                                           final String key,
                                           final S3Client s3Client) {
        log.info("Deleting S3 object {}/{}...", bucketName, key);
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
        log.info("Deleted S3 object {}/{}.", bucketName, key);
    }
}
