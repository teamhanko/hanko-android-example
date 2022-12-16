package io.hanko.hanko_mobile_example.repository

import android.util.Base64
import com.google.android.gms.fido.fido2.api.common.*
import io.hanko.hanko_mobile_example.client.*
import io.hanko.hanko_mobile_example.models.FinalizeResponse

interface WebauthnRepo {
    suspend fun initWebAuthnRegistration(
        token: String, userId: String
    ): PublicKeyCredentialCreationOptions

    suspend fun finalizeWebAuthnRegistration(
        token: String, credential: PublicKeyCredential
    ): FinalizeResponse

    suspend fun initWebAuthnAuthentication(): PublicKeyCredentialRequestOptions
    suspend fun finalizeWebAuthnAuthentication(credential: PublicKeyCredential): Pair<FinalizeResponse, String?>
}

class WebAuthnRepoImpl() : WebauthnRepo {
    private val service by lazy { WebAuthnServiceImpl() }
    override suspend fun initWebAuthnRegistration(
        token: String, userId: String
    ): PublicKeyCredentialCreationOptions {
        val result = service.initWebAuthnRegistration(token, userId)
        val user = PublicKeyCredentialUserEntity(
            Base64.decode(result.publicKey.user.id, Base64.DEFAULT),
            result.publicKey.user.name,
            "",
            result.publicKey.user.displayName ?: result.publicKey.user.name
        )

        val rp = PublicKeyCredentialRpEntity(
            result.publicKey.rp.id, result.publicKey.rp.name, null
        )

        val credParams = result.publicKey.pubKeyCredParams.map {
            if (it.alg != -8) { // workaround, because android does not support this algorithm
                return@map PublicKeyCredentialParameters(it.type, it.alg)
            } else {
                return@map null
            }
        }.filterNotNull()

        val criteriaBuilder = result.publicKey.authenticatorSelection?.let {
            val criteriaBuilder = AuthenticatorSelectionCriteria.Builder()
            it.authenticatorAttachment?.let { at ->
                criteriaBuilder.setAttachment(
                    Attachment.fromString(at)
                )
            }
            criteriaBuilder.setRequireResidentKey(it.requireResidentKey)
            it.residentKey?.let { rk ->
                criteriaBuilder.setResidentKeyRequirement(
                    ResidentKeyRequirement.fromString(rk)
                )
            }
            it.userVerification?.let { uv ->
                criteriaBuilder.setResidentKeyRequirement(
                    ResidentKeyRequirement.fromString(uv)
                )
            }
        }

        val builder = PublicKeyCredentialCreationOptions.Builder()
        builder.setChallenge(
            Base64.decode(result.publicKey.challenge, Base64.DEFAULT)
        )
        builder.setTimeoutSeconds(result.publicKey.timeout?.toDouble())
        builder.setAttestationConveyancePreference(result.publicKey.attestation?.let {
            AttestationConveyancePreference.fromString(it)
        })
        builder.setUser(user)
        builder.setRp(rp)
        builder.setParameters(credParams)
        builder.setAuthenticatorSelection(criteriaBuilder?.build())

        return builder.build()
    }

    override suspend fun finalizeWebAuthnRegistration(
        token: String, credential: PublicKeyCredential
    ): FinalizeResponse {
        val response = credential.response
        if (response is AuthenticatorErrorResponse) {
            throw IllegalArgumentException("credential.response must not contain AuthenticatorErrorResponse")
        } else {
            val resp = response as AuthenticatorAttestationResponse
            return service.finalizeWebAuthnRegistration(
                token, FinishWebAuthnRegistration(
                    id = credential.id,
                    rawId = credential.id,
                    type = credential.type,
                    response = AuthenticatorRegistrationResponse(
                        clientDataJSON = Base64.encodeToString(
                            resp.clientDataJSON,
                            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                        ), attestationObject = Base64.encodeToString(
                            resp.attestationObject,
                            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                        )
                    ),
                    transports = resp.transports
                )
            )
        }
    }

    override suspend fun initWebAuthnAuthentication(): PublicKeyCredentialRequestOptions {
        val result = service.initWebAuthnAuthentication()

        val builder = PublicKeyCredentialRequestOptions.Builder()
        builder.setChallenge(
            Base64.decode(
                result.publicKey.challenge,
                Base64.DEFAULT
            )
        )
        builder.setTimeoutSeconds(result.publicKey.timeout?.toDouble())
        builder.setRpId(result.publicKey.rpId)
        builder.setAllowList(null)

        return builder.build()
    }

    override suspend fun finalizeWebAuthnAuthentication(credential: PublicKeyCredential): Pair<FinalizeResponse, String?> {
        val response = credential.response
        if (response is AuthenticatorErrorResponse) {
            throw IllegalArgumentException("credential.response must not contain AuthenticatorErrorResponse")
        } else {
            val resp = response as AuthenticatorAssertionResponse
            return service.finalizeWebAuthnAuthentication(
                FinishWebAuthnAuthentication(
                    id = credential.id,
                    rawId = credential.id,
                    type = credential.type,
                    response = AuthenticatorAuthenticationResponse(
                        clientDataJson = Base64.encodeToString(
                            resp.clientDataJSON,
                            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                        ),
                        authenticatorData = Base64.encodeToString(
                            resp.authenticatorData,
                            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                        ),
                        signature = Base64.encodeToString(
                            resp.signature,
                            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                        ),
                        userHandle = resp.userHandle?.let {
                            Base64.encodeToString(
                                it,
                                Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                            )
                        }
                    )
                )
            )
        }
    }
}