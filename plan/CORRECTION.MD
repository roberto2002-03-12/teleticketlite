Context: Quarkus 3.37.1 backend, MySQL persistence, running locally in dev mode for testing. Cognito handles identity; local Quarkus instance validates JWTs in-process since there's no external authorizer in this environment. Two modules: auth (claims extraction/authorization) and users (profile management + Cognito registration).

1. `cognito.user-pool-id`, `cognito.client-id`, `cognito.client-secret`

Custom-namespaced properties (not MicroProfile-standard) — they do nothing on their own. They exist only because they're bound via @ConfigProperty injection for use by the AWS SDK v2 CognitoIdentityProviderClient, independent of whatever quarkus.oidc.* does.

Purpose in the `users` module:
- cognito.user-pool-id: required parameter on every Admin API call (AdminCreateUser, AdminAddUserToGroup) — identifies which pool to mutate.
- cognito.client-id / cognito.client-secret: required only if the app computes a SECRET_HASH (HMAC-SHA256 of username + client_id, keyed with client_secret) for client-authenticated calls like SignUp/InitiateAuth. AdminCreateUser itself is IAM-authenticated, not client-authenticated, so these two are irrelevant to that specific call — include them only if a client-facing signup/login flow also exists in this service.

```
@ConfigProperty(name = "cognito.user-pool-id")
String userPoolId;

@Inject
CognitoIdentityProviderClient cognitoClient;

cognitoClient.adminCreateUser(AdminCreateUserRequest.builder()
    .userPoolId(userPoolId)
    .username(email)
    .userAttributes(...)
    .messageAction(MessageActionType.SUPPRESS)
    .build());
```

2. quarkus.oidc.* block

   Configures the quarkus-oidc extension, which performs full in-process OIDC/JWT validation:
   - auth-server-url: base issuer URL Quarkus uses for OIDC discovery — it fetches ${auth-server-url}/.well-known/openid-configuration at startup to locate the JWKS endpoint, token endpoint, etc., automatically. Do not append /.well-known/jwks.json manually here; that's derived, not configured.
   - client-id: the Cognito App Client ID this service authenticates as. For application-type=service, this is mainly used for audience validation if configured; it does not perform a login flow itself.
   - application-type=service: tells Quarkus this is a resource server validating bearer tokens on incoming requests — not a browser-redirect login flow (web-app type). This is the correct mode for an API validating an Authorization: Bearer <token> header.
   - token.issuer: the expected iss claim value the incoming JWT must match exactly. Must be the plain pool URL (https://cognito-idp.{region}.amazonaws.com/{userPoolId}), not a JWKS or discovery path — Quarkus checks this token claim directly against the string in this property.
   - roles.role-claim-path=cognito:groups: overrides Quarkus's default claim path (groups) to read from Cognito's actual group claim, cognito:groups. Without this override, @RolesAllowed checks silently fail since Quarkus looks in a claim that doesn't exist on Cognito tokens.

Critical caveat: Cognito's access token does not carry cognito:groups by default — only the ID token does. The client (Postman/frontend) must send the ID token in the Authorization header during testing, or role-claim-path resolves to nothing and every role check fails with zero groups found — this fails silently and looks like a permissions bug, not a config bug.

```
cognito.user-pool-id=us-east-1_Y3AVPF7cE
cognito.client-id=6m496adbmi95ln3i1pta9tnj9s
cognito.client-secret=126hu4uafeurrdld6c1ofnu0tpjeiprmuasofdnvf6vd9ae72bsu

quarkus.oidc.auth-server-url=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_Y3AVPF7cE
quarkus.oidc.client-id=6m496adbmi95ln3i1pta9tnj9s
quarkus.oidc.application-type=service
quarkus.oidc.token.issuer=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_Y3AVPF7cE
quarkus.oidc.roles.role-claim-path=cognito:groups
```

3. services aren't declared the way you had declared.

- You create an interface class, declare the functions there and then create the implementation class inside `impl` package. The impl class should implement the interface. What you did is creating the function class directly instead of creating an interface and then implementing it.
- Each class should only have one specific class or if it has many then should be related to the purpose group. In this case create 4 classes, the first one would be for user "CLIENT", second user "STAFF", third user "OWNER", fourth user "ADMIN", and fifth class would be general user service like editing profile, something that every user has.

4. Create user petition should be form-data to include image, not JSON with base64. The image should be stored in S3 bucket and the URL should be stored in the database.

These are the variables for S3
```
quarkus.s3.aws.region=us-east-1
quarkus.s3.aws.credentials.type=static
quarkus.s3.aws.credentials.static-provider.access-key-id=ASIAU6GDYID5QRS3PU37
quarkus.s3.aws.credentials.static-provider.secret-access-key=Q3MsQ7s8yXKl0v7lp4uDZE4j4xOObWdTfpN0JszV
s3.bucket-name=teleticket-lite-images
```

example code:
```
@ConfigProperty(name = "s3.bucket-name")
String bucketName;

@Inject
S3Client s3Client; // provisto automáticamente por quarkus-amazon-s3

public String uploadProfilePicture(String userId, byte[] imageBytes, String contentType) {
    String objectKey = "profile-pictures/" + UUID.randomUUID() + ".jpg";

    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .contentType(contentType)
            .build(),
        RequestBody.fromBytes(imageBytes)
    );

    return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, System.getenv("AWS_REGION"), objectKey);
}

public void deleteProfilePicture(String objectKey) {
    s3Client.deleteObject(
        DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .build()
    );
}
```

### NOTES
- in `src/main/java/com/app/teleticket/auth/CurrentUser.java` you only created class `CurrentUser`. First you haven't declared the proper way that I showed in folders infrastructure, you directly just created the class which is WRONG. Secondly, not all functions are used, then what's the purpose of having it? Use it or delete them.