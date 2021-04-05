/* ==================================================================
 * SdkS3Client.java - 3/10/2017 2:11:41 PM
 * 
 * Copyright 2017 SolarNetwork.net Dev Team
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

package net.solarnetwork.common.s3.sdk;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.DeleteObjectsResult.DeletedObject;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import net.solarnetwork.common.s3.S3Client;
import net.solarnetwork.common.s3.S3ObjectMetadata;
import net.solarnetwork.common.s3.S3ObjectRef;
import net.solarnetwork.common.s3.S3ObjectReference;
import net.solarnetwork.settings.SettingSpecifier;
import net.solarnetwork.settings.SettingsChangeObserver;
import net.solarnetwork.settings.support.BaseSettingsSpecifierLocalizedServiceInfoProvider;
import net.solarnetwork.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.support.RemoteServiceException;
import net.solarnetwork.util.ProgressListener;

/**
 * {@link S3Client} using the AWS SDK.
 * 
 * @author matt
 * @version 1.3
 */
public class SdkS3Client extends BaseSettingsSpecifierLocalizedServiceInfoProvider<String>
		implements S3Client, SettingsChangeObserver {

	/** The default value for the {@code regionName} property. */
	public static final String DEFAULT_REGION_NAME = Regions.US_WEST_2.getName();

	/** The default value for the {@code maximumKeysPerRequest} property. */
	public static final int DEFAULT_MAXIMUM_KEYS_PER_REQUEST = 500;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private String accessToken;
	private String accessSecret;
	private String bucketName;
	private String regionName = DEFAULT_REGION_NAME;
	private int maximumKeysPerRequest = DEFAULT_MAXIMUM_KEYS_PER_REQUEST;
	private AWSCredentialsProvider credentialsProvider;
	private AWSCredentialsProvider tokenCredentialsProvider;

	private AmazonS3 s3Client;

	/**
	 * Default constructor.
	 */
	public SdkS3Client() {
		this(SdkS3Client.class.getName());
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *        the settings UID to use
	 */
	public SdkS3Client(String id) {
		super(id);
	}

	@Override
	public synchronized void configurationChanged(Map<String, Object> properties) {
		if ( accessToken != null && accessSecret != null ) {
			tokenCredentialsProvider = new AWSStaticCredentialsProvider(
					new BasicAWSCredentials(accessToken, accessSecret));
		}
		if ( s3Client != null ) {
			s3Client = null;
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

	private synchronized AmazonS3 getClient() {
		AmazonS3 result = s3Client;
		if ( result == null ) {
			AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard().withRegion(regionName);
			AWSCredentialsProvider provider = null;
			if ( credentialsProvider != null && tokenCredentialsProvider != null ) {
				provider = new AWSCredentialsProviderChain(tokenCredentialsProvider,
						credentialsProvider);
			} else if ( tokenCredentialsProvider != null ) {
				provider = tokenCredentialsProvider;
			} else if ( credentialsProvider != null ) {
				provider = credentialsProvider;
			}
			if ( provider != null ) {
				builder.withCredentials(provider);
			}
			result = builder.build();
			s3Client = result;
		}
		return result;
	}

	@Override
	public boolean isConfigured() {
		return (bucketName != null && bucketName.length() > 0 && regionName != null
				&& regionName.length() > 0
				&& (credentialsProvider != null || tokenCredentialsProvider != null));
	}

	@Override
	public Set<S3ObjectReference> listObjects(String prefix) throws IOException {
		AmazonS3 client = getClient();
		Set<S3ObjectReference> result = new LinkedHashSet<>(100);
		try {
			final ListObjectsV2Request req = new ListObjectsV2Request();
			req.setBucketName(bucketName);
			req.setMaxKeys(maximumKeysPerRequest);
			req.setPrefix(prefix);
			ListObjectsV2Result listResult;
			do {
				listResult = client.listObjectsV2(req);
				for ( S3ObjectSummary objectSummary : listResult.getObjectSummaries() ) {
					URL url = client.getUrl(objectSummary.getBucketName(), objectSummary.getKey());
					result.add(new S3ObjectRef(objectSummary.getKey(), objectSummary.getSize(),
							objectSummary.getLastModified(), url));
				}
				req.setContinuationToken(listResult.getNextContinuationToken());
			} while ( listResult.isTruncated() == true );

		} catch ( AmazonServiceException e ) {
			log.warn("AWS error: {}; HTTP code {}; AWS code {}; type {}; request ID {}", e.getMessage(),
					e.getStatusCode(), e.getErrorCode(), e.getErrorType(), e.getRequestId());
			throw new RemoteServiceException("Error listing S3 objects at " + prefix, e);
		} catch ( AmazonClientException e ) {
			log.debug("Error communicating with AWS: {}", e.getMessage());
			throw new IOException("Error communicating with AWS", e);
		}
		if ( log.isDebugEnabled() ) {
			log.debug("Listed {} S3 objects: {}", result.size(),
					result.stream().map(r -> r.getKey()).collect(Collectors.toList()));
		}
		return result;
	}

	@Override
	public String getObjectAsString(String key) throws IOException {
		AmazonS3 client = getClient();
		try {
			String result = client.getObjectAsString(bucketName, key);
			log.debug("Got S3 string {}/{} ({})", bucketName, key, result.length());
			return result;
		} catch ( AmazonServiceException e ) {
			log.warn("AWS error: {}; HTTP code {}; AWS code {}; type {}; request ID {}", e.getMessage(),
					e.getStatusCode(), e.getErrorCode(), e.getErrorType(), e.getRequestId());
			throw new RemoteServiceException("Error getting S3 object at " + key, e);
		} catch ( AmazonClientException e ) {
			log.debug("Error communicating with AWS: {}", e.getMessage());
			throw new IOException("Error communicating with AWS", e);
		}
	}

	@Override
	public <P> net.solarnetwork.common.s3.S3Object getObject(String key,
			ProgressListener<P> progressListener, P progressContext) throws IOException {
		AmazonS3 client = getClient();
		try {
			GetObjectRequest req = new GetObjectRequest(bucketName, key);
			if ( progressListener != null ) {
				SdkTransferProgressListenerAdapter<P> adapter = new SdkTransferProgressListenerAdapter<P>(
						progressListener, progressContext, false);
				req.setGeneralProgressListener(adapter);
			}
			com.amazonaws.services.s3.model.S3Object obj = client.getObject(req);
			log.debug("Got S3 object {}/{} ({})", bucketName, key,
					obj.getObjectMetadata().getContentLength());
			return new SdkS3Object(obj, client.getUrl(bucketName, key));
		} catch ( AmazonServiceException e ) {
			log.warn("AWS error: {}; HTTP code {}; AWS code {}; type {}; request ID {}", e.getMessage(),
					e.getStatusCode(), e.getErrorCode(), e.getErrorType(), e.getRequestId());
			throw new RemoteServiceException("Error getting S3 object at " + key, e);
		} catch ( AmazonClientException e ) {
			log.debug("Error communicating with AWS: {}", e.getMessage());
			throw new IOException("Error communicating with AWS", e);
		}
	}

	@Override
	public URL getObjectURL(String key) {
		AmazonS3 client = getClient();
		return client.getUrl(bucketName, key);
	}

	@Override
	public <P> S3ObjectReference putObject(String key, InputStream in, S3ObjectMetadata objectMetadata,
			ProgressListener<P> progressListener, P progressContext) throws IOException {
		AmazonS3 client = getClient();
		try {
			ObjectMetadata meta = new ObjectMetadata();
			Map<String, ?> customMap = objectMetadata.asCustomMap();
			for ( Map.Entry<String, ?> me : customMap.entrySet() ) {
				meta.setHeader(me.getKey(), me.getValue());
			}
			if ( objectMetadata.getModified() != null ) {
				meta.setLastModified(objectMetadata.getModified());
			}
			meta.setContentLength(objectMetadata.getSize());
			meta.setContentType(objectMetadata.getContentType().toString());
			PutObjectRequest req = new PutObjectRequest(bucketName, key, in, meta);
			if ( progressListener != null ) {
				SdkTransferProgressListenerAdapter<P> adapter = new SdkTransferProgressListenerAdapter<P>(
						progressListener, progressContext, true);
				req.setGeneralProgressListener(adapter);
			}
			if ( objectMetadata.getStorageClass() != null ) {
				req.setStorageClass(objectMetadata.getStorageClass());
			}
			client.putObject(req);
			log.debug("Put S3 object {}/{} ({})", bucketName, key, meta.getContentLength());
			return new S3ObjectRef(key, objectMetadata.getSize(), objectMetadata.getModified(),
					client.getUrl(bucketName, key));
		} catch ( AmazonServiceException e ) {
			log.warn("AWS error: {}; HTTP code {}; AWS code {}; type {}; request ID {}", e.getMessage(),
					e.getStatusCode(), e.getErrorCode(), e.getErrorType(), e.getRequestId());
			throw new RemoteServiceException("Error putting S3 object at " + key, e);
		} catch ( AmazonClientException e ) {
			log.debug("Error communicating with AWS: {}", e.getMessage());
			throw new IOException("Error communicating with AWS", e);
		}
	}

	@Override
	public Set<String> deleteObjects(Iterable<String> keys) throws IOException {
		AmazonS3 client = getClient();
		try {
			DeleteObjectsRequest req = new DeleteObjectsRequest(bucketName).withKeys(
					stream(keys.spliterator(), false).map(k -> new KeyVersion(k)).collect(toList()));
			DeleteObjectsResult res = client.deleteObjects(req);
			List<DeletedObject> deleted = res.getDeletedObjects();
			if ( deleted == null ) {
				return Collections.emptySet();
			}
			return deleted.stream().map(d -> d.getKey())
					.collect(Collectors.toCollection(LinkedHashSet::new));
		} catch ( AmazonServiceException e ) {
			log.warn("AWS error: {}; HTTP code {}; AWS code {}; type {}; request ID {}", e.getMessage(),
					e.getStatusCode(), e.getErrorCode(), e.getErrorType(), e.getRequestId());
			throw new RemoteServiceException("Error deleting S3 objects " + keys, e);
		} catch ( AmazonClientException e ) {
			log.debug("Error communicating with AWS: {}", e.getMessage());
			throw new IOException("Error communicating with AWS", e);
		}
	}

	// SettingsSpecifierProvider

	@Override
	public String getDisplayName() {
		return "AWS SDK S3 Client";
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
	 * Set the credentials provider to authenticate with.
	 * 
	 * <p>
	 * If this is configured, it takes precedence over any configured
	 * {@code accessToken}/{@code accessSecret}.
	 * </p>
	 * 
	 * @param credentialsProvider
	 *        the provider to set
	 */
	public void setCredentialsProvider(AWSCredentialsProvider credentialsProvider) {
		this.credentialsProvider = credentialsProvider;
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
