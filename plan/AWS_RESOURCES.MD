## General resources
- AWS S3
- AWS Cognito
- AWS Lambda
- AWS API Gateway
- AWS RDS

## Resources with SDK for Java
- AWS S3
- AWS Cognito

## Required credentials


## Credentials

### Cognito
- backend
```
cognito.user-pool-id=us-east-1_Y3AVPF7cE
cognito.client-id=6m496adbmi95ln3i1pta9tnj9s
cognito.client-secret=126hu4uafeurrdld6c1ofnu0tpjeiprmuasofdnvf6vd9ae72bsu

quarkus.oidc.auth-server-url=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_Y3AVPF7cE
quarkus.oidc.client-id=6m496adbmi95ln3i1pta9tnj9s
quarkus.oidc.application-type=service
quarkus.oidc.token.issuer=https://cognito-idp.us-east-1.amazonaws.com/6m496adbmi95ln3i1pta9tnj9s
quarkus.oidc.roles.role-claim-path=cognito:groups
```

Todas las variables
```
{
    "UserPoolClient": {
        "UserPoolId": "us-east-1_Y3AVPF7cE",
        "ClientName": "teleticketlite-project",
        "ClientId": "6m496adbmi95ln3i1pta9tnj9s",
        "ClientSecret": "126hu4uafeurrdld6c1ofnu0tpjeiprmuasofdnvf6vd9ae72bsu",
        "LastModifiedDate": "2026-07-06T04:44:48.462000+00:00",
        "CreationDate": "2026-07-06T04:44:48.462000+00:00",
        "RefreshTokenValidity": 30,
        "TokenValidityUnits": {},
        "ExplicitAuthFlows": [
            "ALLOW_USER_PASSWORD_AUTH",
            "ALLOW_REFRESH_TOKEN_AUTH"
        ],
        "AllowedOAuthFlowsUserPoolClient": false,
        "EnableTokenRevocation": true,
        "EnablePropagateAdditionalUserContextData": false,
        "AuthSessionValidity": 3
    }
}
```

- frontend
```
clientId: "4cb918blib3uv4hv9sla8v4lhh"
```

Todas las variables
```
{
    "UserPoolClient": {
        "UserPoolId": "us-east-1_Y3AVPF7cE",
        "ClientName": "teleticket-lite-react",
        "ClientId": "4cb918blib3uv4hv9sla8v4lhh",
        "LastModifiedDate": "2026-07-06T05:08:29.047000+00:00",
        "CreationDate": "2026-07-06T05:08:29.047000+00:00",
        "RefreshTokenValidity": 30,
        "TokenValidityUnits": {},
        "ExplicitAuthFlows": [
            "ALLOW_USER_SRP_AUTH",
            "ALLOW_REFRESH_TOKEN_AUTH"
        ],
        "AllowedOAuthFlowsUserPoolClient": false,
        "EnableTokenRevocation": true,
        "EnablePropagateAdditionalUserContextData": false,
        "AuthSessionValidity": 3
    }
}
```
