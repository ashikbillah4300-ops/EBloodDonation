# 🌐 EBloodDonation Backend — Online Deployment Guide (অনলাইনে ডিপ্লয় নির্দেশিকা)

এই ব্যাকএন্ডটি সম্পূর্ণ বিনামূল্যে (100% Free) অনলাইনে হোস্ট করার জন্য সবচেয়ে সহজ মাধ্যম হলো **Render.com** অথবা **Railway** এবং ফ্রি PostgreSQL ডাটাবেজের জন্য **Neon.tech** অথবা **Supabase**।

---

### 🚀 পদ্ধতি ১: Render.com এ ২ মিনিটে ফ্রি ডিপ্লয়মেন্ট (সবচেয়ে সহজ)

#### ধাপ ১: ফ্রি PostgreSQL ডাটাবেজ তৈরি করুন (Neon.tech বা Supabase)
1. **[Neon.tech](https://neon.tech)** এ গিয়ে সাইন আপ করুন।
2. নতুন একটি প্রোজেক্ট তৈরি করুন (নাম দিন `eblood-db`)।
3. ড্যাশবোর্ডে **Connection Details** থেকে `postgres://...` কানেকশন স্ট্রিংটি কপি করুন।

#### ধাপ ২: GitHub-এ কোড পুশ করুন
1. আপনার গিট রিপোজিটোরিতে `backend/` ফোল্ডারসহ প্রোজেক্টটি পুশ করুন।

#### ধাপ ৩: Render.com এ ডিপ্লয় করুন
1. **[Render.com](https://render.com)** এ লগইন করুন।
2. **"New +"** -> **"Web Service"** চাপুন।
3. আপনার GitHub রিপোজিটোরি কানেক্ট করুন।
4. কনফিগারেশন দিন:
   - **Root Directory:** `backend`
   - **Environment:** `Node`
   - **Build Command:** `npm install`
   - **Start Command:** `node src/server.js`
   - **Instance Type:** Free
5. **Environment Variables** সেকশনে নিচের কীগুলো যোগ করুন:
   - `DATABASE_URL`: *(ধাপ ১-এ Neon.tech থেকে কপি করা স্ট্রিংটি পেস্ট করুন)*
   - `JWT_SECRET`: `eblood_super_secret_jwt_key_2026`
   - `DEFAULT_ADMIN_USERNAME`: `admin`
   - `DEFAULT_ADMIN_PASSWORD`: `eblood@2026`
6. **"Deploy Web Service"** বাটনে চাপুন।
7. ডিপ্লয় শেষ হলে Render আপনাকে একটি লাইভ HTTPS লিংক দিবে (যেমন: `https://eblood-api.onrender.com`)।

---

### 📱 মোবাইল অ্যাপের সাথে অনলাইন ব্যাকএন্ড কানেক্ট করার নিয়ম:

1. **মোবাইল অ্যাপটি ওপেন করুন**।
2. **Profile / Settings** -> **"🛡️ Admin Portal / প্রশাসনিক প্যানেল"**-এ যান।
3. অ্যাডমিন পাসওয়ার্ড দিয়ে লগইন করুন (ডিফল্ট: `admin` / `eblood@2026`)।
4. **"🌐 Online Server & Sync"** ট্যাবে যান।
5. আপনার লাইভ সার্ভার URL দিন (যেমন: `https://eblood-api.onrender.com`)।
6. **"⚡ Test Connection"** বাটনে ট্যাপ করলেই অ্যাপ সার্ভারের সাথে পিং করে স্ট্যাটাস ও রেসপন্স টাইম দেখাবে।
7. **"🔄 Sync Now"** বাটনে ট্যাপ করলে অনলাইন সার্ভারের বিকাশ অনুদান নম্বর ও নোটিশ সরাসরি মোবাইল অ্যাপে লাইভ সিঙ্ক হয়ে যাবে!
