# Axle Weather App - Copilot Instructions

## 📋 Project Overview

This is a modern Android weather application undergoing a comprehensive modernization from legacy Material Components to Material Design 3 with Jetpack Compose integration. The primary goal is to eliminate crashes in the widget configuration system and create a robust, maintainable codebase.

## 🎯 Current Status

**✅ COMPLETED:**
- Phase 1: Foundation Modernization (Material 3, Compose setup, build system)
- Phase 2: Widget Configuration UI (Jetpack Compose implementation)

**🔄 IN PROGRESS:**
- Phase 3: Widget Implementation with Glance

**📋 PENDING:**
- Phase 4: Main App UI Enhancement
- Phase 5: Testing & Polish

## 🏗️ Architecture Decisions

### **Technology Stack:**
- **Language:** Kotlin (target JVM 17)
- **UI Framework:** Jetpack Compose + Legacy Views (transitional)
- **Architecture:** MVVM with Repository pattern
- **Widget System:** Glance for App Widgets
- **Design System:** Material Design 3
- **Network:** Retrofit with OkHttp
- **Async:** Kotlin Coroutines
- **Build System:** Gradle with Compose BOM

### **Key Components:**
```
├── Data Layer
│   ├── API Service (Retrofit - Axle Weather API)
│   ├── Repository (WeatherRepository)
│   └── Models (WeatherResponse, etc.)
├── Domain Layer
│   └── ViewModels (WeatherViewModel, WeatherWidgetConfigViewModel)
├── UI Layer
│   ├── Activities (MainActivity, WeatherWidgetConfigActivity)
│   ├── Compose Screens (Widget Config)
│   ├── Legacy Views (Main Activity - to be enhanced)
│   └── Theme System (Material 3)
└── Widget Layer
    ├── WeatherGlanceWidget (Modern Glance-based)
    └── WeatherWidgetProvider (Legacy - to be phased out)
```

## 🎨 Design Guidelines

### **Material 3 Compliance:**
- Use semantic color tokens (primary, secondary, tertiary)
- Dynamic color support for Android 12+
- Proper light/dark theme implementation
- Material 3 component usage (Cards, Buttons, Typography)

### **Compose Best Practices:**
- State hoisting pattern
- Composition over inheritance
- Remember and mutable state management
- Proper lifecycle awareness
- Accessibility considerations

### **Widget Development:**
- Use Glance for modern widget implementation
- Per-widget configuration storage
- Proper error handling and loading states
- Samsung One UI compatibility

## 🔧 Development Standards

### **Code Quality:**
- Follow Kotlin coding conventions
- Use descriptive variable names (camelCase)
- Class names in PascalCase
- Comprehensive JSDoc/KDoc comments
- Error handling with proper logging
- Unit tests for business logic

### **File Organization:**
- Package by feature when possible
- Clear separation of concerns
- Consistent naming conventions
- Remove unused files during cleanup

### **Dependencies Management:**
- Use Compose BOM for version alignment
- Keep dependencies up-to-date
- Prefer AndroidX libraries
- Use latest stable versions

## 🚨 Critical Issues Addressed

### **Widget Configuration Crashes:**
- **Root Cause:** Legacy view inflation issues
- **Solution:** Complete Compose migration
- **Status:** ✅ Fixed with WeatherWidgetConfigActivityCompose.kt

### **Theme Inconsistencies:**
- **Root Cause:** Mixed Material 2/3 components
- **Solution:** Complete Material 3 migration
- **Status:** ✅ Completed

### **Build System Outdated:**
- **Root Cause:** Old Gradle configurations
- **Solution:** Modern build.gradle with Compose
- **Status:** ✅ Updated

## 📱 API Integration

### **Axle Weather API:**
- **Base URL:** `https://api.axle.coffee/api/`
- **Endpoint:** `/weather` (GET)
- **Response:** WeatherResponse with current/hourly/forecast data
- **Error Handling:** Comprehensive error states
- **Timeout:** 30 seconds connection/read

### **Data Models:**
- WeatherResponse (root)
- Location, WeatherData, CurrentWeather
- HourlyWeather, Forecast structures
- Proper Gson serialization

## 🧪 Testing Strategy

### **Test Coverage Goals:**
- Unit Tests: ViewModels, Repository, Utils
- Integration Tests: API calls, Database operations
- UI Tests: Compose components, User flows
- Widget Tests: Configuration and updates

### **Quality Metrics:**
- Code coverage >80%
- Zero crashes in production
- Accessibility score 100%
- Performance benchmarks

## 🔄 Migration Strategy

### **Incremental Approach:**
1. Foundation first (themes, build system)
2. Critical components (widget config)
3. Core features (widget implementation)
4. UI enhancements (main activity)
5. Testing and polish

### **Backward Compatibility:**
- Maintain existing APIs during transition
- Legacy widget support during migration
- Gradual component replacement

## 📋 TODO Tracking

### **Immediate Priorities:**
- [ ] Complete Glance widget implementation
- [ ] Update AndroidManifest for Glance receiver
- [ ] Test widget configuration flow
- [ ] Remove unused legacy files

### **Phase 3 Checklist:**
- [ ] WeatherGlanceWidget complete implementation
- [ ] Configuration integration with Glance
- [ ] Data loading optimization
- [ ] Error handling in widget
- [ ] Update manifest and resources

### **Future Enhancements:**
- [ ] MainActivity Compose migration
- [ ] Performance optimizations
- [ ] Accessibility improvements
- [ ] Advanced widget features

## 🔍 Code Review Checklist

### **Before Submitting:**
- [ ] All new code follows Material 3 guidelines
- [ ] Proper error handling implemented
- [ ] Documentation updated
- [ ] No unused imports or files
- [ ] Accessibility considerations addressed
- [ ] Performance implications considered

### **Review Criteria:**
- Architecture consistency
- Material 3 compliance
- Code readability and maintainability
- Error handling completeness
- Test coverage adequacy

## 📚 Resources

### **Documentation:**
- [Material Design 3](https://m3.material.io/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Glance App Widgets](https://developer.android.com/jetpack/androidx/releases/glance)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)

### **Key Files to Monitor:**
- `build.gradle` (app level)
- `AndroidManifest.xml`
- Theme files (`themes.xml`, `colors.xml`)
- Widget provider and configuration files
- Compose theme system

---

**Last Updated:** July 2, 2025
**Current Phase:** Phase 3 - Widget Implementation with Glance
**Next Milestone:** Complete Glance widget and test configuration flow
