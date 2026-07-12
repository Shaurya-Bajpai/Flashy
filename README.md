# 🔦 Flashy - Smart Flash Notifications for Android

<div align="center">
<!--   <img src="https://img.shields.io/badge/Version-1.0.0-blue?style=for-the-badge" alt="Version"> -->
  <a href="https://github.com/Shaurya-Bajpai/Flashy/releases/download/v1.0/app-debug.apk">
    <img src="https://img.shields.io/badge/Download-APK-green?style=for-the-badge&logo=android" alt="Download APK">
  </a>
<!--
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen?style=for-the-badge&logo=android" alt="Android Platform">
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge" alt="License">
  <img src="https://img.shields.io/badge/Battery-Optimized-orange?style=for-the-badge&logo=battery" alt="Battery Optimized">
-->
</div>

<div align="center">
  <h3>🚀 Transform Your Phone's Flashlight Into The Ultimate Notification System</h3>
  <p><em>Never miss another important alert with intelligent flash notifications that adapt to your lifestyle</em></p>
</div>

---

## 🌟 What is Flashy?

**Flashy** is a revolutionary Android application that transforms your phone's flashlight into an intelligent visual notification system. Built with accessibility, customization, and battery efficiency at its core, Flashy ensures you never miss important alerts even when your phone is on silent mode.

### 🎯 Perfect For:
- 🔇 **Silent Mode Users** - Stay connected without disturbing others
- 👂 **Hearing Impaired** - Visual alerts for complete accessibility
- 🎮 **Gamers & Content Creators** - Never miss calls during streams
- 🏢 **Professional Environments** - Discreet notification system
- 🌙 **Night Shift Workers** - Visual alerts in quiet environments

---

## ✨ Key Features

### 🔔 **Smart Notification Detection**
- 📞 **Incoming Calls** - Customizable flash patterns for different contacts
- 💬 **SMS & Messages** - Instant visual alerts for text messages
- 📱 **App Notifications** - Support for WhatsApp, Telegram, Instagram, and 50+ apps
- 🎵 **Media Controls** - Flash alerts for music and video notifications
- 📧 **Email Alerts** - Never miss important emails again

<!---
### ⚙️ **Advanced Customization**
- 🎛️ **Adjustable Blink Speed** - From subtle pulses to rapid flashes
- 🌈 **Pattern Library** - Choose from 10+ pre-built flash patterns
- 🎨 **Custom Patterns** - Create your own unique flash sequences
- 📊 **Intensity Control** - Adjust brightness from whisper-soft to super-bright
- ⏱️ **Duration Settings** - Control how long notifications flash
-->
  
### 🧠 **Intelligent Rules Engine**
- 🔒 **Screen State Aware** - Only flash when screen is off
- 🌙 **Do Not Disturb Integration** - Respects your quiet hours
- 🔋 **Battery Level Rules** - Disable flash when battery is low
<!--
- 📍 **Location-Based** - Different rules for home, work, and travel
- 👥 **Contact Prioritization** - VIP contacts get special flash patterns
-->
### 🛡️ **Privacy & Security**
- 🔐 **Notification Content Stays Local** - We never read or store what your notifications say
- 📊 **Anonymized Usage Analytics** - Firebase Analytics/Crashlytics track feature usage and crashes, keyed to an anonymous device ID, not your identity
- 🔒 **Encrypted Settings** - Your preferences are secure
- 🌐 **Network Used for Premium & Analytics** - Required for Razorpay checkout and the analytics above; core flash features work offline

---

## 🚀 Quick Start Guide

### 📋 **Requirements**
- 📱 Android 6.0 (API level 23) or higher
- 🔦 Device with LED flash/flashlight
- 💾 ~5MB storage space
- 🔋 Minimal battery usage (< 1% per day)

### 📥 **Installation**

<!--
#### Option 1: Google Play Store (Recommended)
```bash
# Search for "Flashy Flash Notifications" in Google Play Store
# Or use the direct link: [Coming Soon]
```
-->

#### Option 1: APK Installation
```bash
# Download the latest APK from our releases page
# Enable "Unknown Sources" in Android Settings
# Install the APK file
```

🔗 [Download APK](https://github.com/Shaurya-Bajpai/Flashy/releases/download/v1.0/app-debug.apk)

#### Option 2: Build from Source
```bash
git clone https://github.com/Shaurya-Bajpai/flashy.git
cd flashy
./gradlew assembleDebug
```

### 🔧 **Initial Setup**

1. **📱 Launch Flashy** - Open the app after installation
2. **🔓 Grant Permissions** - Allow notification access and camera permissions
3. **🎯 Enable App Notification** - Select which apps should trigger flash notifications
4. **⚡ Test Flash** - Use the built-in test feature to verify functionality
<!--
5. **🎨 Customize** - Adjust patterns, speed, and rules to your preference
-->
---

## 🎮 User Interface & Navigation

### 🏠 **Main Dashboard**
- 📊 **Quick Stats** - Daily notification count and battery usage
- 🔄 **Quick Toggle** - Enable/disable flash notifications instantly
- 🎛️ **Pattern Preview** - See your current flash pattern in action
- 🔋 **Battery Monitor** - Real-time battery impact tracking

<!--
### 📱 **App Selection Screen**
- 📋 **Installed Apps List** - All notification-capable apps
- 🔍 **Search & Filter** - Find apps quickly
- 🎨 **Per-App Patterns** - Different flash patterns for different apps
- 📈 **Usage Statistics** - See which apps notify you most

### ⚙️ **Advanced Settings**
- 🎛️ **Pattern Editor** - Create custom flash sequences
- 🕐 **Schedule Rules** - Time-based flash behavior
- 🔋 **Battery Optimization** - Fine-tune power consumption
- 🌍 **Accessibility Options** - Enhanced features for users with disabilities
-->

---

<!--
## 🔧 Configuration Options

### 🎨 **Flash Patterns**

| Pattern Name | Description | Use Case |
|-------------|-------------|----------|
| 🔦 **Single Flash** | One quick flash | Subtle notifications |
| 💫 **Double Pulse** | Two quick flashes | Text messages |
| 🌟 **Strobe** | Rapid continuous flashing | Important calls |
| 🔄 **Heartbeat** | Rhythmic pulsing | Social media |
| 🌈 **Custom** | User-defined pattern | Personal preference |

### 📊 **Intensity Levels**

```
Level 1: ░░░░░░░░░░ (10%) - Whisper Mode
Level 2: ██░░░░░░░░ (25%) - Gentle Alert
Level 3: ████░░░░░░ (50%) - Standard Mode
Level 4: ██████░░░░ (75%) - Attention Mode
Level 5: ██████████ (100%) - Emergency Mode
```

### 🕐 **Smart Rules Examples**

```yaml
# Example Rule Configurations
VIP_Contacts:
  pattern: "Strobe"
  intensity: 100%
  duration: 10s
  
Work_Hours:
  enabled: true
  time: "9:00 AM - 5:00 PM"
  pattern: "Single Flash"
  intensity: 25%
  
Sleep_Mode:
  enabled: true
  time: "11:00 PM - 7:00 AM"
  flash_enabled: false
  
Low_Battery:
  threshold: 15%
  action: "Disable Flash"
```

---

-->

## 🔋 Battery Optimization

### 📈 **Power Consumption**
- 🟢 **Idle Mode**: < 0.1% per day
- 🟡 **Active Mode**: 0.5-1% per day
- 🔴 **Heavy Usage**: 1-2% per day (100+ notifications)

### ⚡ **Optimization Features**
<!--
- 🔋 **Adaptive Brightness** - Automatically adjusts based on ambient light
-->
- 🕐 **Smart Scheduling** - Reduces activity during low-usage hours
- 📱 **Screen State Detection** - Only flashes when screen is off
- 🔄 **Background Optimization** - Minimal CPU usage
- 💾 **Memory Efficient** - Uses < 10MB RAM

### 🛠️ **Battery Saving Tips**

<!--
✅ Enable "Battery Saver Mode" in Flashy settings
✅ Limit flash duration to 3-5 seconds
-->
```
✅ Use lower intensity levels (15-30%)
✅ Use "Screen Off Only" mode
✅ Disable flash for less important apps
```

---
<!--
## 🎯 Accessibility Features

### 👂 **Hearing Impaired Support**
- 🔊 **Visual Replacements** - Flash patterns replace audio alerts
- 🎨 **High Contrast** - Enhanced visual distinction
- 📱 **Vibration Sync** - Combine flash with vibration patterns
- 🔔 **Alert Persistence** - Longer flash durations for important alerts

### 👁️ **Visual Impairment Support**
- 🗣️ **Voice Announcements** - TTS integration for pattern names
- 🔍 **Large Text** - Scalable UI elements
- 🎯 **Touch Targets** - Larger buttons and controls
- 🔊 **Audio Feedback** - Confirmation sounds for actions

### 🧠 **Cognitive Accessibility**
- 📝 **Simple Interface** - Clean, intuitive design
- 🎯 **One-Touch Actions** - Easy enable/disable
- 📖 **Help System** - Built-in tutorials and guides
- 🔄 **Consistent Patterns** - Predictable behavior

---
-->

## 🔒 Privacy & Security

### 🛡️ **Data Protection**
- 🚫 **No PII Collected** - No name, email, or notification content is ever stored
- 📊 **Anonymized Analytics** - Firebase Analytics/Crashlytics and a Firestore record (device model, app version, premium status) are keyed to an anonymous device ID, not your identity — see `privacy_policy.html`
- 🔐 **Local-First Settings** - All preferences saved on device via DataStore
- 🌐 **Network Required for Premium** - Razorpay checkout and purchase restore need connectivity; core flash features work offline
- 🔒 **Encrypted Preferences** - Settings protected with device encryption

### 🔍 **Permissions Explained**
```
📷 Camera Permission: Required to control flashlight
🔔 Notification Access: Required to detect incoming notifications
🔋 Battery Optimization: Optional for better performance
📱 Device Admin: Optional for advanced power management
```

### 🛡️ **Security Features**
- 🔐 **App Lock** - Secure settings with PIN/Pattern/Fingerprint
- 👤 **Guest Mode** - Temporary disable for privacy
- 🔒 **Safe Mode** - Emergency disable option
- 🛡️ **Malware Protection** - Built-in security scanning

---

## 🤝 Contributing

We welcome contributions from the community! Here's how you can help:

### 🐛 **Bug Reports**
```markdown
Found a bug? Please report it:
1. Check existing issues first
2. Use the bug report template
3. Include device information
4. Describe reproduction steps
```

### 💡 **Feature Requests**
```markdown
Have an idea? We'd love to hear it:
1. Check the roadmap first
2. Use the feature request template
3. Explain the use case
4. Provide mockups if possible
```

### 🔧 **Code Contributions**
```bash
# Fork the repository
git clone https://github.com/Shaurya-Bajpai/flashy.git

# Create a feature branch
git checkout -b feature/amazing-feature

# Make your changes
# Test thoroughly
# Submit a pull request
```

### 📚 **Documentation**
- 📝 Improve README files
- 🌐 Translate to other languages
- 📖 Write tutorials and guides
- 🎥 Create video demonstrations

---
<!--
## 🗺️ Roadmap

### 🔮 **Coming Soon** (v1.1)
- 🎵 **Music Integration** - Flash to beat/rhythm
- 🌈 **Color Flash** - RGB LED support for compatible devices
- 🤖 **AI Patterns** - Machine learning for optimal flash patterns
- 📊 **Analytics Dashboard** - Detailed usage insights

### 🚀 **Future Plans** (v1.2+)
- ⌚ **Smartwatch Sync** - Coordinate with wearable devices
- 🏠 **Smart Home** - Integrate with IoT devices
- 🔗 **IFTTT Support** - Trigger flash from external apps
- 🌐 **Web Dashboard** - Configure settings from computer

### 💭 **Community Requested**
- 🎮 **Gaming Mode** - Special patterns for gaming notifications
- 📱 **Multi-Device** - Sync across multiple phones
- 🎨 **Theme Engine** - Custom app themes
- 🔊 **Sound Sync** - Combine with audio alerts

---

## 📞 Support & Help

### 🆘 **Getting Help**
- 📚 **Documentation** - Check our comprehensive wiki
- 💬 **Community Forum** - Join discussions with other users
- 📧 **Email Support** - support@flashyapp.com
- 🐦 **Social Media** - Follow @FlashyApp for updates
-->

### 🔧 **Troubleshooting**

#### Common Issues:
```
❌ Flash not working?
✅ Check camera permissions
✅ Ensure notification access is granted
✅ Verify flashlight isn't used by other apps

❌ High battery usage?
✅ Reduce flash intensity
✅ Enable "Screen Off Only" mode
✅ Limit notification sources

❌ App crashes?
✅ Update to latest version
✅ Restart device
✅ Clear app cache
```

### 📱 **Device Compatibility**
- ✅ **Fully Supported** - Samsung Galaxy, Google Pixel, OnePlus
- ✅ **Mostly Supported** - Xiaomi, Huawei, LG, Sony
- ⚠️ **Limited Support** - Older devices (Android 5.0 and below)
- ❌ **Not Supported** - Devices without LED flash

---

## 📄 License

This project is licensed under the Apache License 2.0 License - see the [LICENSE](LICENSE) file for details.

```
Copyright [2025] [Shaurya Bajpai]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

## 🙏 Acknowledgments
<!--
### 👥 **Contributors**
- 🏆 **Lead Developer** - Shaurya Bajpai
- 🎨 **UI/UX Designer** - Shaurya Bajpai
- 🔧 **Beta Testers** - Our amazing community
- 🌐 **Translators** - Making Flashy accessible worldwide
-->
### 📚 **Libraries & Tools**
- 🔦 **CameraX** - Camera API for flash control
- 🔔 **NotificationListenerService** - Notification detection
- 🎨 **Material Design** - UI components
- 🔋 **Battery Optimization** - Power management utilities

### 💡 **Inspiration**
Special thanks to the accessibility community for inspiring us to create a tool that makes smartphones more inclusive for everyone.

---

<div align="center">
  <h3>🌟 Star us on GitHub if you love Flashy! 🌟</h3>
  <p>Made with ❤️ by Shaurya Bajpai</p>
  
  <a href="https://github.com/Shaurya-Bajpai/flashy.git">
    <img src="https://img.shields.io/badge/GitHub-★ Star-yellow?style=for-the-badge&logo=github" alt="Star on GitHub">
  </a>
  <!--
  <a href="https://play.google.com/store/apps/details?id=com.flashyapp">
    <img src="https://img.shields.io/badge/Google Play-Download-green?style=for-the-badge&logo=google-play" alt="Download on Google Play">
  </a>
  -->
</div>

---

**💫 Transform your notifications. Transform your life. Get Flashy today!**
