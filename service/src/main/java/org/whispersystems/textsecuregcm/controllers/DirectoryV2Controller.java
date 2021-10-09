/*
 * Copyright 2013-2021 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package org.whispersystems.textsecuregcm.controllers;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import java.util.Base64;
import java.util.UUID;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.whispersystems.textsecuregcm.auth.AuthenticatedAccount;
import org.whispersystems.textsecuregcm.auth.ExternalServiceCredentialGenerator;
import org.whispersystems.textsecuregcm.auth.ExternalServiceCredentials;
import org.whispersystems.textsecuregcm.util.ByteUtil;
import org.whispersystems.textsecuregcm.util.UUIDUtil;
import org.whispersystems.textsecuregcm.util.Util;

@Path("/v2/directory")
public class DirectoryV2Controller {

  private final ExternalServiceCredentialGenerator directoryServiceTokenGenerator;

  public DirectoryV2Controller(ExternalServiceCredentialGenerator userTokenGenerator) {
    this.directoryServiceTokenGenerator = userTokenGenerator;
  }

  @Timed
  @GET
  @Path("/auth")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAuthToken(@Auth AuthenticatedAccount auth) {

    final UUID uuid = auth.getAccount().getUuid();
    final String e164 = auth.getAccount().getNumber();
    final long e164AsLong = Long.parseLong(e164, e164.indexOf('+'), e164.length() - 1, 10);

    final byte[] uuidAndNumber = ByteUtil.combine(UUIDUtil.toBytes(uuid), Util.longToByteArray(e164AsLong));
    final String username = Base64.getEncoder().encodeToString(uuidAndNumber);

    final ExternalServiceCredentials credentials = directoryServiceTokenGenerator.generateFor(username);

    return Response.ok().entity(credentials).build();
  }
}
