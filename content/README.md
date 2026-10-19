# Content Repository

هذا المجلد معزول عن كود التطبيق. عند تحديث النصوص أو إضافة مصدر جديد، يُحدّث المصدر و`catalog.json` ثم يُبنى التطبيق تلقائيًا من assets دون تعديل شاشات القراءة.

## المصدر الحالي

- Dataset: `quran-json`
- Script: `uthmani`
- Chapters: 114
- Verses: 6236
- Source URL: https://quran-json.risanb.com/
- Upstream source: Tanzil Project
- License: CC BY 3.0 للنص Tanzil؛ يجب إبقاء notice المصدر وعدم تغيير النص.
- Downloaded file: `sources/quran-json/quran-uthmani.json`
- Metadata: `sources/quran-json/chapters.json`
- Attribution/manifest: `sources/quran-json/manifest.json`

## قاعدة تحديث المحتوى

لا تعدّل `quran-uthmani.json` يدويًا. شغّل أداة التحديث، ثم تحقّق من العدد والـ SHA-256 والمصدر والترخيص. كل حزمة تدخل التطبيق عبر `catalog.json` وتحتوي على category وtitle وsourceFile وlicense وsourceUrl.

النص الأصلي محفوظ كما وصل من المصدر. التطبيع يُنشأ داخل طبقة البيانات للبحث فقط ولا يُعرض بدل النص الأصلي.
