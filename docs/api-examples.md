# API örnekleri

Örneklerde talep eden kullanıcı header'ları:

```text
X-User-Id: maker01
X-User-Name: Talep Oluşturan
X-User-Roles: PARAMETER_EDITOR,PARAMETER_VIEWER
```

## Metadata ve filtreli sorgu

```http
GET /api/parameter-resources/LOAN_PRODUCT/metadata
```

```http
POST /api/parameter-resources/LOAN_PRODUCT/search
Content-Type: application/json

{
  "filters": [
    { "field": "status", "operator": "EQ", "value": "ACTIVE" },
    { "field": "name", "operator": "CONTAINS", "value": "spot" }
  ],
  "page": 0,
  "size": 20,
  "sort": [{ "field": "name", "direction": "ASC" }]
}
```

## Update talebi

```http
POST /api/change-requests
Content-Type: application/json

{
  "title": "Spot kredi ad güncellemesi",
  "description": "Ürün adı standarda getiriliyor.",
  "items": [{
    "resourceCode": "LOAN_PRODUCT",
    "operation": "UPDATE",
    "recordId": "TL_SPOT",
    "newValue": { "name": "Ticari TL Spot Kredi" },
    "executionOrder": 10
  }]
}
```

## Onay

Onaycı farklı bir kullanıcı olmalı:

```http
POST /api/change-requests/1/approve
X-User-Id: approver01
X-User-Name: Onaycı Kullanıcı
X-User-Roles: PARAMETER_APPROVER,PARAMETER_VIEWER
Content-Type: application/json

{ "note": "Kontrol edildi." }
```

