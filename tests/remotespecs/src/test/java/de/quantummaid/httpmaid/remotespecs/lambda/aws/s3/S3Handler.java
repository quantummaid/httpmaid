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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

import static com.amazonaws.services.s3.AmazonS3ClientBuilder.defaultClient;

@Slf4j
public final class S3Handler {

    private S3Handler() {
    }

    public static String uploadToS3Bucket(final String bucketName,
                                          final File file) {
        final String key = keyFromFile(file);
        final AmazonS3 amazonS3 = defaultClient();
        try {
            log.info("Uploading {} to S3 object {}/{}...", file, bucketName, key);
            if (!fileNeedsUploading(bucketName, key, amazonS3)) {
                log.info("S3 object with matching MD5 already present, skipping upload.");
            } else {
                log.info("S3 object not already present, uploading...");
                final PutObjectRequest request = new PutObjectRequest(bucketName, key, file);
                amazonS3.putObject(request);
                log.info("Uploaded {} to S3 object {}/{}.", file, bucketName, key);
            }
            return key;
        } finally {
            amazonS3.shutdown();
        }
    }

    private static boolean fileNeedsUploading(final String bucketName,
                                              final String key,
                                              final AmazonS3 amazonS3) {
        try {
            amazonS3.getObjectMetadata(bucketName, key);
            return true;
        } catch (final AmazonS3Exception e) {
            if (!e.getMessage().startsWith("Not Found")) {
                throw e;
            }
            return true;
        }
    }

    private static String keyFromFile(final File file) {
        final Md5Checksum newContentMD5 = Md5Checksum.ofFile(file);
        return newContentMD5.getValue();
    }

    public static void deleteAllObjectsInBucket(final String bucketName) {
        final AmazonS3 amazonS3 = defaultClient();
        try {
            final ObjectListing objectListing = amazonS3.listObjects(bucketName);
            objectListing.getObjectSummaries().forEach(s3ObjectSummary -> {
                final String key = s3ObjectSummary.getKey();
                deleteFromS3Bucket(bucketName, key, amazonS3);
            });
        } finally {
            amazonS3.shutdown();
        }
    }

    private static void deleteFromS3Bucket(final String bucketName,
                                           final String key,
                                           final AmazonS3 amazonS3) {
        log.info("Deleting S3 object {}/{}...", bucketName, key);
        final DeleteObjectRequest request = new DeleteObjectRequest(bucketName, key);
        amazonS3.deleteObject(request);
        log.info("Deleted S3 object {}/{}.", bucketName, key);
    }
}
