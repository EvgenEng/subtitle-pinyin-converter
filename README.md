# 🎬 Subtitle Pinyin Converter

A production-ready Spring Boot application that converts Chinese SRT subtitles into Pinyin.

---

## 🚀 Features

- Convert Chinese characters → Pinyin
- Supports multiple output formats:
   - WITH_SPACES → `ni hao`
   - WITHOUT_SPACES → `nihao`
   - WITH_TONES → `nǐ hǎo`
   - CAPITALIZED → `Ni Hao`
- Multiple subtitle modes:
   - ORIGINAL
   - PINYIN
   - DUAL (Chinese + Pinyin)
   - TRIPLE (Chinese + Pinyin + translation-ready)
- Preserves SRT structure and timing
- File upload via REST API
- File validation endpoint
- Download converted subtitles
- Global exception handling
- Clean API response structure
- UTF-8 support
- Unit tested

---

## 🧱 Tech Stack

- Java 17
- Spring Boot 3.x
- Maven
- Pinyin4j
- Caffeine Cache
- JUnit 5

---

## ⚙️ Getting Started

### 1. Clone the project

```bash
git clone https://github.com/EvgenEng/subtitle-pinyin-converter.git
cd subtitle-pinyin-converter
```

### 2. Build
```bash
mvn clean package
```

### 3. Run
```bash
mvn spring-boot:run
```

or

```bash
java -jar target/subtitle-pinyin-converter-1.0.0.jar
```

## 🔧 Configuration

Edit application.yml:

```yaml
subtitle:
max-file-size: 10485760
upload:
path: uploads
converted:
path: converted
pinyin:
cache-size: 500
```

## 📡 API

### 🔹 Convert subtitle

POST /api/v1/subtitle/convert

Request (multipart/form-data):

file – SRT file
pinyinFormat – WITH_SPACES / WITHOUT_SPACES / WITH_TONES / CAPITALIZED
mode – ORIGINAL / PINYIN / DUAL / TRIPLE
convertNonChinese – true/false

Example:
```bash
curl -X POST http://localhost:8080/api/v1/subtitle/convert \
-F "file=@test.srt" \
-F "pinyinFormat=WITH_SPACES"
```

Response:
```json
{
"success": true,
"message": "File successfully converted to Pinyin",
"data": {
"fileName": "test_pinyin.srt",
"originalFileName": "test.srt",
"blockCount": 2,
"conversionTime": "2026-04-02T13:57:17",
"downloadUrl": "/api/v1/subtitle/download/test_pinyin.srt"
}
}
```

### 🔹 Validate file

POST /api/v1/subtitle/validate


### 🔹 Download file

GET /api/v1/subtitle/download/{fileName}

### 🔹 Health check

GET /api/v1/subtitle/health

## 🧪 Testing
```bash
mvn test
```

## 📂 Project Structure
src/
├── main/java/com/subtitle/
│   ├── controller/
│   ├── service/
│   ├── parser/
│   ├── exception/
│   ├── config/
│   └── util/
└── test/

## 🧠 Design Highlights
- Constructor-based dependency injection
- Generic API response wrapper (ApiResponse<T>)
- Custom exception system with error codes
- Clean separation: controller → service → parser
- Input validation using Jakarta Validation
- Regex-based SRT parsing
- Caching for performance

## 📊 Performance
- Handles files up to 10MB
- ~1000 subtitle blocks/sec
- Cache significantly reduces repeated conversions

## 🛣️ Roadmap
- Integration tests (MockMvc)
- Support for .ass and .vtt
- Docker support
- Cloud deployment
- Batch processing
- Web UI