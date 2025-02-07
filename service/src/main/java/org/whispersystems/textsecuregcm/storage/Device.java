/*
 * Copyright 2013 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package org.whispersystems.textsecuregcm.storage;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.whispersystems.textsecuregcm.auth.SaltedTokenHash;
import org.whispersystems.textsecuregcm.entities.ECSignedPreKey;
import org.whispersystems.textsecuregcm.identity.IdentityType;

public class Device {

  public static final byte PRIMARY_ID = 1;
  public static final byte MAXIMUM_DEVICE_ID = Byte.MAX_VALUE;
  public static final int MAX_REGISTRATION_ID = 0x3FFF;
  public static final List<Byte> ALL_POSSIBLE_DEVICE_IDS = IntStream.range(Device.PRIMARY_ID, MAXIMUM_DEVICE_ID).boxed()
      .map(Integer::byteValue).collect(Collectors.toList());

  @JsonDeserialize(using = DeviceIdDeserializer.class)
  @JsonProperty
  private byte id;

  @JsonProperty
  private String  name;

  @JsonProperty
  private String  authToken;

  @JsonProperty
  private String  salt;

  @JsonProperty
  private String  gcmId;

  @JsonProperty
  private String  apnId;

  @JsonProperty
  private String  voipApnId;

  @JsonProperty
  private long pushTimestamp;

  @JsonProperty
  private long uninstalledFeedback;

  @JsonProperty
  private boolean fetchesMessages;

  @JsonProperty
  private int registrationId;

  @Nullable
  @JsonProperty("pniRegistrationId")
  private Integer phoneNumberIdentityRegistrationId;

  @JsonProperty
  private ECSignedPreKey signedPreKey;

  @JsonProperty("pniSignedPreKey")
  private ECSignedPreKey phoneNumberIdentitySignedPreKey;

  @JsonProperty
  private long lastSeen;

  @JsonProperty
  private long created;

  @JsonProperty
  private String userAgent;

  @JsonProperty
  private DeviceCapabilities capabilities;

  public String getApnId() {
    return apnId;
  }

  public void setApnId(String apnId) {
    this.apnId = apnId;

    if (apnId != null) {
      this.pushTimestamp = System.currentTimeMillis();
    }
  }

  public String getVoipApnId() {
    return voipApnId;
  }

  public void setVoipApnId(String voipApnId) {
    this.voipApnId = voipApnId;
  }

  public void setUninstalledFeedbackTimestamp(long uninstalledFeedback) {
    this.uninstalledFeedback = uninstalledFeedback;
  }

  public long getUninstalledFeedbackTimestamp() {
    return uninstalledFeedback;
  }

  public void setLastSeen(long lastSeen) {
    this.lastSeen = lastSeen;
  }

  public long getLastSeen() {
    return lastSeen;
  }

  public void setCreated(long created) {
    this.created = created;
  }

  public long getCreated() {
    return this.created;
  }

  public String getGcmId() {
    return gcmId;
  }

  public void setGcmId(String gcmId) {
    this.gcmId = gcmId;

    if (gcmId != null) {
      this.pushTimestamp = System.currentTimeMillis();
    }
  }

  public byte getId() {
    return id;
  }

  public void setId(byte id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setAuthTokenHash(SaltedTokenHash credentials) {
    this.authToken = credentials.hash();
    this.salt      = credentials.salt();
  }

  /**
   * Has this device been manually locked?
   *
   * We lock a device by prepending "!" to its token.
   * This character cannot normally appear in valid tokens.
   *
   * @return true if the credential was locked, false otherwise.
   */
  public boolean hasLockedCredentials() {
    SaltedTokenHash auth = getAuthTokenHash();
    return auth.hash().startsWith("!");
  }

  /**
   * Lock device by invalidating authentication tokens.
   *
   * This should only be used from Account::lockAuthenticationCredentials.
   *
   * See that method for more information.
   */
  public void lockAuthTokenHash() {
    SaltedTokenHash oldAuth = getAuthTokenHash();
    String token = "!" + oldAuth.hash();
    String salt = oldAuth.salt();
    setAuthTokenHash(new SaltedTokenHash(token, salt));
  }

  public SaltedTokenHash getAuthTokenHash() {
    return new SaltedTokenHash(authToken, salt);
  }

  @Nullable
  public DeviceCapabilities getCapabilities() {
    return capabilities;
  }

  public void setCapabilities(DeviceCapabilities capabilities) {
    this.capabilities = capabilities;
  }

  public boolean isEnabled() {
    boolean hasChannel = fetchesMessages || StringUtils.isNotEmpty(getApnId()) || StringUtils.isNotEmpty(getGcmId());

    return (id == PRIMARY_ID && hasChannel && signedPreKey != null) ||
           (id != PRIMARY_ID && hasChannel && signedPreKey != null && lastSeen > (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)));
  }

  public boolean getFetchesMessages() {
    return fetchesMessages;
  }

  public void setFetchesMessages(boolean fetchesMessages) {
    this.fetchesMessages = fetchesMessages;
  }

  public boolean isPrimary() {
    return getId() == PRIMARY_ID;
  }

  public int getRegistrationId() {
    return registrationId;
  }

  public void setRegistrationId(int registrationId) {
    this.registrationId = registrationId;
  }

  public OptionalInt getPhoneNumberIdentityRegistrationId() {
    return phoneNumberIdentityRegistrationId != null ? OptionalInt.of(phoneNumberIdentityRegistrationId) : OptionalInt.empty();
  }

  public void setPhoneNumberIdentityRegistrationId(final int phoneNumberIdentityRegistrationId) {
    this.phoneNumberIdentityRegistrationId = phoneNumberIdentityRegistrationId;
  }

  public ECSignedPreKey getSignedPreKey(final IdentityType identityType) {
    return switch (identityType) {
      case ACI -> signedPreKey;
      case PNI -> phoneNumberIdentitySignedPreKey;
    };
  }

  public void setSignedPreKey(ECSignedPreKey signedPreKey) {
    this.signedPreKey = signedPreKey;
  }

  public void setPhoneNumberIdentitySignedPreKey(final ECSignedPreKey phoneNumberIdentitySignedPreKey) {
    this.phoneNumberIdentitySignedPreKey = phoneNumberIdentitySignedPreKey;
  }

  public long getPushTimestamp() {
    return pushTimestamp;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  public String getUserAgent() {
    return this.userAgent;
  }

  public record DeviceCapabilities(boolean storage, boolean transfer, boolean pni, boolean paymentActivation) {
  }
}
