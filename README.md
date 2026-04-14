# 🎬 Subtitle Pinyin Converter

Production-ready Spring Boot application that converts Chinese SRT subtitles into Pinyin and optionally translates them into Russian.

---

## 🚀 Features

### 🔤 Pinyin Conversion
- Chinese → Pinyin conversion
- Multiple formats:
  - WITH_SPACES → `ni hao`
  - WITHOUT_SPACES → `nihao`
  - WITH_TONES → `nǐ hǎo`
  - CAPITALIZED → `Ni Hao`

### 🎞 Subtitle Modes
- ORIGINAL
- PINYIN
- DUAL (Chinese + Pinyin)
- TRIPLE (Chinese + Pinyin + Russian translation)

### 🌍 Translation (DeepSeek)
- Batch translation support
- Async processing (parallel chunks)
- Retry with backoff
- Fallback if API unavailable (`[RU] text`)

### ⚡ Performance
- Batch processing of subtitles
- Parallel translation execution
- Caffeine caching for Pinyin
- Handles large files efficiently

### 🧰 Core Features
- Preserves SRT structure & timing
- File upload via REST API
- Download converted subtitles
- File validation endpoint
- Global exception handling
- Clean API response structure
- UTF-8 support

---

## 🧱 Tech Stack

- Java 17
- Spring Boot 3.x
- Maven
- WebClient (Reactive HTTP)
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

deepseek:
  api:
    url: https://api.deepseek.com/v1/chat/completions
    key: ${DEEPSEEK_API_KEY}
    model: deepseek-chat
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
-F "mode=TRIPLE"
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

## 🧠 Architecture
    Controller → Service → Parser → Formatter → Storage
                              ↓
                     TranslationService
                              ↓
                        DeepSeek API




### Key Design Decisions:
- Constructor-based DI
- Clear service separation
- Batch + async translation
- Resilient external API calls (retry + fallback)
- Clean error handling via custom exceptions

### 📊 Performance
- Handles files up to 10MB
- ~1000 subtitle blocks/sec
- Parallel translation improves throughput ×5–10
- Cache reduces repeated computations

## ⚠️ Fallback Behavior

If DeepSeek API is unavailable or returns an error:

    [RU] original text

Application never crashes due to translation failure.

## 🧪 Testing
```bash

mvn test
```

## 📂 Project Structure
    src/
    ├── main/java/com/subtitle/
    │   ├── config/        # configuration (WebClient, filters, etc.)
    │   ├── controller/    # REST controllers
    │   ├── service/       # business logic
    │   ├── parser/        # SRT parsing
    │   ├── exception/     # error handling
    │   └── util/          # utilities
    │
    └── test/java/com/subtitle/
                        ├── controller/
                        ├── service/
                        └── parser/

## 🛣️ Roadmap
- Integration tests (MockMvc)
- Support for .ass and .vtt
- Docker support
- Cloud deployment
- Translation cache (reduce API cost 💸)
- Web UI

## 🐳 Run with Docker

- docker build -t subtitle-app .
- docker run -p 8080:8080 -e DEEPSEEK_API_KEY=your_key subtitle-app