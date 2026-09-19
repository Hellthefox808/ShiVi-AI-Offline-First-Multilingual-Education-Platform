# VoxBridge AI — API Versioning & Deprecation Policy

## 1. Versioning Strategy & URI Semantics

VoxBridge AI enforces **Major URI Path Versioning** (`/v1`, `/v2`) paired with strict semantic backward compatibility guarantees.

```
https://api.voxbridge.ai/v1/sessions   <-- Active Production API
https://api.voxbridge.ai/v2/sessions   <-- Future Major Version (Under Multi-Year Evolution)
```

---

## 2. Backward Compatibility Guarantees (Non-Breaking Changes in `/v1`)

The following modifications are deemed non-breaking and are introduced directly into the existing `/v1` path without requiring client migration:
1. **Additive JSON Fields:** Adding new optional keys to request payloads or new informative fields to response payloads.
2. **New Supported Languages / Voices:** Adding support for additional speech languages, locales, or neural voice models.
3. **New Endpoints:** Exposing entirely new REST routes (e.g., `/v1/glossaries/audit`).
4. **New Query Parameters:** Adding optional query filters to collection endpoints.

### 2.1 Breaking Changes (Triggering `/v2`)
Any of the following changes strictly mandates a new major version:
- Renaming or deleting an existing JSON request/response property.
- Changing the data type of an existing field (e.g., integer to string).
- Introducing a new required parameter to an existing endpoint.
- Modifying binary streaming header magic bytes or framing offsets.

---

## 3. Deprecation Lifecycle & IETF RFC 8594 Compliance

When an API version or specific endpoint is slated for retirement:

```
[ACTIVE] ──► [DEPRECATION ANNOUNCED] ──► [SUNSETTING PERIOD (12 MONTHS)] ──► [FORMAL SUNSET (410 GONE)]
```

### 3.1 Standard HTTP Sunset Headers
Deprecated endpoints inject IETF RFC 8594 compliance headers into every response:

```http
HTTP/2 200 OK
Content-Type: application/json
Deprecation: @1762819200
Sunset: Wed, 11 Nov 2026 00:00:00 GMT
Link: <https://docs.voxbridge.ai/migrations/v2>; rel="sunset"; title="VoxBridge v2 Migration Guide"
```

---

## 4. Proactive Deprecation Telemetry & Developer Outreaches

1. **Traffic Tracking:** Prometheus meters call volume on deprecated endpoints by tenant:
   `sum(rate(http_requests_deprecated_total{endpoint, tenant_id}[1d])) by (tenant_id)`.
2. **Automated Warning Emails:** When a tenant's API keys make calls to an endpoint scheduled for sunset within 90 days, automated weekly notification emails are dispatched to the organization's technical admins with migration code samples.
