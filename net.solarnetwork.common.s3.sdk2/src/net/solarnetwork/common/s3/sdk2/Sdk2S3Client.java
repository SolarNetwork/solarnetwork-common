/* ==================================================================
 * Sdk2S3Client.java - 16/06/2024 5:26:40 pm
 *
 * Copyright 2024 SolarNetwork.net Dev Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.common.s3.sdk2;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.solarnetwork.common.s3.S3Client;
import net.solarnetwork.common.s3.S3Object;
import net.solarnetwork.common.s3.S3ObjectMetadata;
import net.solarnetwork.common.s3.S3ObjectRef;
import net.solarnetwork.common.s3.S3ObjectReference;
import net.solarnetwork.service.ProgressListener;
import net.solarnetwork.service.RemoteServiceException;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.SettingsChangeObserver;
import net.solarnetwork.settings.support.BaseSettingsSpecifierLocalizedServiceInfoProvider;
import net.solarnetwork.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.util.ObjectUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.client.config.SdkAdvancedAsyncClientOption;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.http.nio.netty.NettySdkAsyncHttpService;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.Download;
import software.amazon.awssdk.transfer.s3.model.DownloadRequest;
import software.amazon.awssdk.transfer.s3.model.Upload;
import software.amazon.awssdk.transfer.s3.model.UploadRequest;

/**
 * {@link S3Client} using the AWS SDK V2.
 *
 * @author matt
 * @version 2.0
 */
public class Sdk2S3Client extends BaseSettingsSpecifierLocalizedServiceInfoProvider
		implements S3Client, SettingsChangeObserver {

	/** The default value for the {@code regionName} property. */
	public static final String DEFAULT_REGION_NAME = Region.US_WEST_2.id();

	/** The default value for the {@code maximumKeysPerRequest} property. */
	public static final int DEFAULT_MAXIMUM_KEYS_PER_REQUEST = 500;

	/** The metadata map key for a content disposition value. */
	public static final String CONTENT_DISPOSITION_KEY = "Content-Disposition";

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final ExecutorService executorService;

	private String accessToken;
	private String accessSecret;
	private String bucketName;
	private String regionName = DEFAULT_REGION_NAME;
	private int maximumKeysPerRequest = DEFAULT_MAXIMUM_KEYS_PER_REQUEST;
	private AwsCredentialsProvider credentialsProvider;

	private AwsCredentialsProvider tokenCredentialsProvider;
	private S3AsyncClient s3Client;
	private S3TransferManager s3TransferManager;

	/**
	 * Default constructor.
	 */
	public Sdk2S3Client(ExecutorService executorService) {
		this(executorService, Sdk2S3Client.class.getName());
	}

	/**
	 * Constructor.
	 *
	 * @param id
	 *        the settings UID to use
	 */
	public Sdk2S3Client(ExecutorService executorService, String id) {
		super(id);
		this.executorService = ObjectUtils.requireNonNullArgument(executorService, "executorService");
	}

	@Override
	public synchronized void configurationChanged(Map<String, Object> properties) {
		if ( accessToken != null && accessSecret != null ) {
			tokenCredentialsProvider = StaticCredentialsProvider
					.create(AwsBasicCredentials.create(accessToken, accessSecret));
		}
		if ( s3TransferManager != null ) {
			try {
				s3TransferManager.close();
			} catch ( Exception e ) {
				log.warn("Error closing S3TransferManager for {}/{} token {}", regionName, bucketName,
						accessToken, e);
			} finally {
				s3TransferManager = null;
			}
		}
		if ( s3Client != null ) {
			try {
				s3Client.close();
			} catch ( Exception e ) {
				log.warn("Error closing S3Client for {}/{} token {}", regionName, bucketName,
						accessToken, e);
			} finally {
				s3Client = null;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("S3Client{region=");
		builder.append(regionName);
		builder.append(",bucket=");
		builder.append(bucketName);
		builder.append("}");
		return builder.toString();
	}

	@Override
	public boolean isConfigured() {
		return (bucketName != null && bucketName.length() > 0 && regionName != null
				&& regionName.length() > 0
				&& (credentialsProvider != null || tokenCredentialsProvider != null));
	}

	private <T> T performAction(String description,
			BiFunction<S3AsyncClient, S3TransferManager, CompletableFuture<T>> action)
			throws IOException {
		final S3AsyncClient client;
		final S3TransferManager mgr;
		synchronized ( this ) {
			client = getClient();
			mgr = s3TransferManager;
		}
		try {
			return action.apply(client, mgr).get();
		} catch ( ExecutionException | CancellationException | InterruptedException e ) {
			Throwable cause = e.getCause();
			if ( cause instanceof SdkClientException ) {
				SdkClientException ex = (SdkClientException) cause;
				log.debug("Error communicating with AWS: {}", ex.getMessage());
				throw new IOException("Error communicating with AWS", e);
			} else if ( cause instanceof AwsServiceException ) {
				AwsServiceException ex = (AwsServiceException) cause;
				log.warn("AWS error: {}; HTTP code {}; AWS code {}; service {}", ex.getMessage(),
						ex.statusCode(), ex.awsErrorDetails().errorCode(),
						ex.awsErrorDetails().serviceName());
			} else if ( e instanceof CancellationException || e instanceof InterruptedException ) {
				log.debug("AWS action was cancelled.");
			}
			throw new RemoteServiceException("Error " + description, e);
		}
	}

	@Override
	public Set<S3ObjectReference> listObjects(String prefix) throws IOException {
		Set<S3ObjectReference> result = new LinkedHashSet<>(100);
		return performAction("listing S3 objects at " + prefix, (client, xfer) -> {
			final ListObjectsV2Request.Builder req = ListObjectsV2Request.builder().bucket(bucketName)
					.maxKeys(maximumKeysPerRequest).prefix(prefix);
			CompletableFuture<Set<S3ObjectReference>> f = new CompletableFuture<>();
			ListObjectsV2Response listResult;
			do {
				try {
					listResult = client.listObjectsV2(req.build()).get();
				} catch ( Exception e ) {
					Throwable cause = e.getCause();
					f.completeExceptionally(cause);
					return f;
				}
				for ( software.amazon.awssdk.services.s3.model.S3Object obj : listResult.contents() ) {
					GetUrlRequest getReq = GetUrlRequest.builder().bucket(bucketName).key(obj.key())
							.build();
					URL url = client.utilities().getUrl(getReq);
					result.add(
							new S3ObjectRef(obj.key(), obj.size(), Date.from(obj.lastModified()), url));
				}
				req.continuationToken(listResult.nextContinuationToken());
			} while ( listResult.isTruncated() == true );
			if ( log.isDebugEnabled() ) {
				log.debug("Listed {} S3 objects: {}", result.size(),
						result.stream().map(r -> r.getKey()).collect(Collectors.toList()));
			}
			f.complete(result);
			return f;
		});
	}

	@Override
	public String getObjectAsString(String key) throws IOException {
		return performAction("getting S3 object at " + key, (client, xfer) -> {
			return client
					.getObject(r -> r.bucket(bucketName).key(key), AsyncResponseTransformer.toBytes())
					.thenApplyAsync(res -> {
						String result = res.asUtf8String();
						log.debug("Got S3 string {}/{} ({})", bucketName, key, result.length());
						return result;
					});
		});
	}

	@Override
	public URL getObjectURL(String key) {
		try {
			return performAction("get object URL", (client, xfer) -> {
				return CompletableFuture
						.completedFuture(client.utilities().getUrl(r -> r.bucket(bucketName).key(key)));
			});
		} catch ( IOException e ) {
			throw new RemoteServiceException("Error composing object URL for key " + key, e);
		}
	}

	@Override
	public <P> S3Object getObject(String key, ProgressListener<P> progressListener, P progressContext)
			throws IOException {
		return performAction("getting S3 object at " + key, (client, xfer) -> {
			final URL url = client.utilities().getUrl(r -> r.bucket(bucketName).key(key));

			DownloadRequest.TypedBuilder<ResponseInputStream<GetObjectResponse>> builder = DownloadRequest
					.builder().getObjectRequest(r -> r.bucket(bucketName).key(key))
					.responseTransformer(AsyncResponseTransformer.toBlockingInputStream());
			if ( progressListener != null ) {
				builder = builder.addTransferListener(
						new Sdk2TransferListenerAdapter<P>(progressListener, progressContext));
			}

			DownloadRequest<ResponseInputStream<GetObjectResponse>> downloadRequest = builder.build();

			Download<ResponseInputStream<GetObjectResponse>> download = xfer.download(downloadRequest);

			return download.completionFuture().thenApplyAsync(result -> {
				log.debug("Got S3 object {}/{}", bucketName, key);
				return new Sdk2S3Object(result.result(), url);
			});
		});
	}

	@Override
	public <P> S3ObjectReference putObject(String key, InputStream in, S3ObjectMetadata objectMetadata,
			ProgressListener<P> progressListener, P progressContext) throws IOException {
		return performAction("putting S3 object at " + key, (client, xfer) -> {
			final URL url = client.utilities().getUrl(r -> r.bucket(bucketName).key(key));

			UploadRequest.Builder builder = UploadRequest.builder().putObjectRequest(r -> {
				r.bucket(bucketName).key(key);

				Map<String, ?> customMap = objectMetadata.asCustomMap();
				if ( !customMap.isEmpty() ) {
					Map<String, String> meta = new LinkedHashMap<>(customMap.size());
					for ( Map.Entry<String, ?> me : customMap.entrySet() ) {
						// Content-Disposition handled directly
						if ( CONTENT_DISPOSITION_KEY.equalsIgnoreCase(me.getKey()) ) {
							r.contentDisposition(me.getValue().toString());
						} else {
							meta.put(me.getKey(), me.getValue().toString());
						}
					}
					if ( !meta.isEmpty() ) {
						r.metadata(meta);
					}
				}

				if ( objectMetadata.getContentType() != null ) {
					r.contentType(objectMetadata.getContentType().toString());
				}
				if ( objectMetadata.getStorageClass() != null ) {
					r.storageClass(objectMetadata.getStorageClass());
				}
			}).requestBody(
					AsyncRequestBody.fromInputStream(in, objectMetadata.getSize(), executorService));

			if ( progressListener != null ) {
				builder = builder.addTransferListener(
						new Sdk2TransferListenerAdapter<P>(progressListener, progressContext));
			}

			UploadRequest uploadRequest = builder.build();

			Upload upload = xfer.upload(uploadRequest);

			return upload.completionFuture().thenApplyAsync(result -> {
				log.debug("Put S3 object {}/{}", bucketName, key);
				return new S3ObjectRef(key, objectMetadata.getSize(), objectMetadata.getModified(), url);
			});
		});
	}

	@Override
	public Set<String> deleteObjects(Iterable<String> keys) throws IOException {
		return performAction("deleting S3 objects " + keys, (client, xfer) -> {
			List<ObjectIdentifier> idents = new ArrayList<>(8);
			for ( String key : keys ) {
				idents.add(ObjectIdentifier.builder().key(key).build());
			}
			if ( idents.isEmpty() ) {
				return CompletableFuture.completedFuture(Collections.emptySet());
			}

			Delete del = Delete.builder().objects(idents).build();

			DeleteObjectsRequest r = DeleteObjectsRequest.builder().bucket(bucketName).delete(del)
					.build();

			CompletableFuture<DeleteObjectsResponse> f = client.deleteObjects(r);

			return f.thenApplyAsync(result -> {
				log.debug("Delete S3 objects {}/{}", bucketName, keys);
				return result.deleted().stream().map(o -> o.key())
						.collect(Collectors.toCollection(LinkedHashSet::new));
			});
		});
	}

	private synchronized S3AsyncClient getClient() {
		S3AsyncClient client = this.s3Client;
		if ( client == null ) {
			S3AsyncClientBuilder builder = S3AsyncClient.builder();
			if ( regionName != null && !regionName.isEmpty() ) {
				builder.region(Region.of(regionName));
			}
			builder.httpClient(new NettySdkAsyncHttpService().createAsyncHttpClientFactory().build());
			builder.asyncConfiguration(async -> {
				async.advancedOption(SdkAdvancedAsyncClientOption.FUTURE_COMPLETION_EXECUTOR,
						executorService);
			});
			AwsCredentialsProvider provider = null;
			if ( credentialsProvider != null && tokenCredentialsProvider != null ) {
				provider = AwsCredentialsProviderChain.of(tokenCredentialsProvider, credentialsProvider);
			} else if ( tokenCredentialsProvider != null ) {
				provider = tokenCredentialsProvider;
			} else if ( credentialsProvider != null ) {
				provider = credentialsProvider;
			}
			if ( provider != null ) {
				builder.credentialsProvider(provider);
			}

			client = builder.build();
			this.s3Client = client;

			this.s3TransferManager = S3TransferManager.builder().s3Client(client).build();
		}
		return client;
	}

	// SettingsSpecifierProvider

	@Override
	public String getDisplayName() {
		return "AWS SDKv2 S3 Client";
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		List<SettingSpecifier> result = new ArrayList<>(5);
		result.add(new BasicTextFieldSettingSpecifier("accessToken", ""));
		result.add(new BasicTextFieldSettingSpecifier("accessSecret", "", true));
		result.add(new BasicTextFieldSettingSpecifier("regionName", DEFAULT_REGION_NAME));
		result.add(new BasicTextFieldSettingSpecifier("bucketName", ""));
		result.add(new BasicTextFieldSettingSpecifier("maximumKeysPerRequest",
				String.valueOf(DEFAULT_MAXIMUM_KEYS_PER_REQUEST)));
		return result;
	}

	// Accessors

	/**
	 * Set the bucket name to connect to.
	 *
	 * @param bucketName
	 *        the bucketName to set
	 */
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	/**
	 * Set the AWS region to use.
	 *
	 * @param regionName
	 *        the region name to set; defaults to us-west-2
	 */
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	/**
	 * Set the maximum number of S3 object keys to request in one request.
	 *
	 * @param maximumKeysPerRequest
	 *        the maximum to set
	 */
	public void setMaximumKeysPerRequest(int maximumKeysPerRequest) {
		this.maximumKeysPerRequest = maximumKeysPerRequest;
	}

	/**
	 * Set the AWS access token to use.
	 *
	 * @param accessToken
	 *        the access token to set
	 */
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	/**
	 * Set the AWS access token secret to use.
	 *
	 * @param accessSecret
	 *        the access secret to set
	 */
	public void setAccessSecret(String accessSecret) {
		this.accessSecret = accessSecret;
	}

}
