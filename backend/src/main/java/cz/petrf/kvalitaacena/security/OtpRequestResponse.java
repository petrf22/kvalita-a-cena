package cz.petrf.kvalitaacena.security;

import java.util.UUID;

public record OtpRequestResponse(UUID challengeUid, long expiresInSec, long resendAfterSec) {
}
