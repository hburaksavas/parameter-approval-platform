# Parameter Approval Platform

Spring Boot 3.4 + Oracle ve React/MUI ile geliştirilmiş metadata tabanlı ürün parametre yönetim
platformu. Generic CRUD, bulk işlemler ve ilişkili tablo create işlemleri maker-checker onayından
geçmeden canlı tablolara yazılmaz.

## Özellikler

- `@ParameterResource` ve `@ParameterField` ile whitelist tabanlı generic CRUD talebi
- `@FilterField` ile React'ta otomatik filtre üretimi ve güvenli Criteria API sorgusu
- Tek talepte CREATE, UPDATE, DELETE ve bulk işlem
- `clientReference` ile parent-child/aggregate create
- Old/new JSON snapshot ve alan bazlı onay karşılaştırması
- `@Version` ile onay anında optimistic conflict kontrolü
- Talep seviyesinde atomik onay: bütün item'lar commit veya rollback olur
- Maker-checker ayrımı: talebi oluşturan kullanıcı karar veremez
- Kayıtlı `CustomQueryProvider` ile join/projection/özel filtre ekranları
- Oracle Flyway migration'ları ve örnek `LoanProduct` / `LoanRate` modeli
- Header tabanlı kurumsal kullanıcı entegrasyonu

## Proje yapısı

```text
parameter-approval-platform/
├── backend/        Spring Boot API ve onay motoru
├── frontend/       React + JavaScript + MUI uygulaması
├── docs/           Mimari ve API örnekleri
└── .github/        Backend/frontend CI
```

Detaylar: [Mimari](docs/architecture.md) · [API örnekleri](docs/api-examples.md)

## Gereksinimler

- Java 17+
- Maven 3.9+
- Node.js 22+
- Oracle Database 19c+
- Uygulama şemasında tablo, sequence, index ve constraint oluşturma yetkisi

## Oracle ayarları

```bash
export DB_URL='jdbc:oracle:thin:@//localhost:1521/FREEPDB1'
export DB_USERNAME='parameter_app'
export DB_PASSWORD='change-me'
```

Migration'lar başlangıçta Flyway tarafından çalıştırılır. Var olan kurumsal şemada Flyway ayrı bir
deployment adımı olarak kullanılıyorsa `spring.flyway.enabled=false` verilebilir.

## Çalıştırma

Backend:

```bash
mvn -pl backend spring-boot:run
```

Frontend:

```bash
cd frontend
npm ci
npm run dev
```

Uygulama `http://localhost:5173` adresinde açılır. Demo kullanıcı seçicisi maker, approver ve viewer
rollerini ayrı kullanıcılarla denemeyi sağlar. Kurumsal ortamda:

```bash
VITE_DEMO_AUTH=false npm run build
```

## Kurumsal güvenlik notu

Backend kullanıcıyı `X-User-Id`, `X-User-Name` ve `X-User-Roles` header'larından alır. Bu servis
doğrudan istemciye açılmamalıdır. Gateway dışarıdan gelen bu header'ları silmeli, kimliği doğrulanmış
kullanıcı bilgisiyle yeniden üretmeli ve backend yalnızca gateway ağından/mTLS üzerinden erişilebilir
olmalıdır. Frontend'deki demo kullanıcı seçicisi production build'de kapatılmalıdır.

## Önemli tasarım sınırları

- Demo basit `@Id` alanlarını destekler. Composite ID için ayrı `IdCodec` stratejisi eklenmelidir.
- Çok büyük importlar iş talebi olarak parçalara ayrılabilir; onay atomikliği talep sınırındadır.
- Silme sırası `executionOrder` ile child → parent verilmelidir.
- Serbest SQL veya istemciden entity/kolon sınıf adı kabul edilmez.
- Hassas alanlar `@ParameterField(sensitive = true)` ile response içinde maskelenir.

## Test

```bash
mvn test
cd frontend
npm run lint
npm test
npm run build
```

