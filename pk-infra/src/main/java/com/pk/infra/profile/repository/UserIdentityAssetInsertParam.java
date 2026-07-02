package com.pk.infra.profile.repository;
public class UserIdentityAssetInsertParam {
    private long profileId; private String mobileNo; private long profileVersionId; private String idCardHash;
    private String idCardCiphertext; private byte[] idCardNonce; private byte[] idCardTag; private String fullName;
    private String idCardImageEncryptedRef; private String facePhotoImageEncryptedRef; private String encryptionKeyRef;
    private String ocrChannel; private String ocrResultJson;
    public UserIdentityAssetInsertParam(long profileId, String mobileNo, long profileVersionId, String idCardHash,
            String idCardCiphertext, byte[] idCardNonce, byte[] idCardTag, String fullName,
            String idCardImageEncryptedRef, String facePhotoImageEncryptedRef, String encryptionKeyRef,
            String ocrChannel, String ocrResultJson) {
        this.profileId = profileId; this.mobileNo = mobileNo; this.profileVersionId = profileVersionId;
        this.idCardHash = idCardHash; this.idCardCiphertext = idCardCiphertext; this.idCardNonce = idCardNonce;
        this.idCardTag = idCardTag; this.fullName = fullName; this.idCardImageEncryptedRef = idCardImageEncryptedRef;
        this.facePhotoImageEncryptedRef = facePhotoImageEncryptedRef; this.encryptionKeyRef = encryptionKeyRef;
        this.ocrChannel = ocrChannel; this.ocrResultJson = ocrResultJson;
    }
    public long getProfileId() { return profileId; } public String getMobileNo() { return mobileNo; }
    public long getProfileVersionId() { return profileVersionId; } public String getIdCardHash() { return idCardHash; }
    public String getIdCardCiphertext() { return idCardCiphertext; } public byte[] getIdCardNonce() { return idCardNonce; }
    public byte[] getIdCardTag() { return idCardTag; } public String getFullName() { return fullName; }
    public String getIdCardImageEncryptedRef() { return idCardImageEncryptedRef; }
    public String getFacePhotoImageEncryptedRef() { return facePhotoImageEncryptedRef; }
    public String getEncryptionKeyRef() { return encryptionKeyRef; } public String getOcrChannel() { return ocrChannel; }
    public String getOcrResultJson() { return ocrResultJson; }
}
