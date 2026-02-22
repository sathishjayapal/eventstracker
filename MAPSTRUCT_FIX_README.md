# MapStruct Bean Not Found - Quick Fix Guide

## 🔴 The Error You're Seeing

```
Parameter 1 of constructor in me.sathish.event_service.domain.DomainService 
required a bean of type 'me.sathish.event_service.domain.DomainMapper' 
that could not be found.
```

## 🔍 What's Wrong

MapStruct needs to **generate implementation classes** for your mapper interfaces, but they're not being created. Spring can't find the `DomainMapper` bean because `DomainMapperImpl` doesn't exist yet.

## ✅ What I Fixed

I updated your `pom.xml` with:

1. **MapStruct compiler arguments** - Tells MapStruct to generate Spring components
2. **Build Helper Plugin** - Adds generated sources to the build path

## 🚀 How to Fix It NOW

### Quick Fix (Run This)

```bash
cd /Users/sathishjayapal/IdeaProjects/eventstracker

# Option 1: Use the script I created
./generate-mappers.sh

# Option 2: Manual commands
mvn clean compile
mvn spring-boot:run
```

### Expected Result

After `mvn compile`, you should see:

```
target/generated-sources/annotations/
└── me/sathish/event_service/
    ├── domain/
    │   └── DomainMapperImpl.java          ← Spring @Component
    └── domain_event/
        └── DomainEventMapperImpl.java     ← Spring @Component
```

## 🛠️ If Using IntelliJ IDEA

The IDE needs to know about the generated sources:

### Method 1: Enable Annotation Processing (Recommended)

1. **Settings** (Cmd+, on Mac, Ctrl+Alt+S on Windows/Linux)
2. **Build, Execution, Deployment → Compiler → Annotation Processors**
3. ✅ Check **Enable annotation processing**
4. Set **Production sources directory**: `target/generated-sources/annotations`
5. Click **Apply** and **OK**

### Method 2: Reload Maven Project

1. Open **Maven** tool window (View → Tool Windows → Maven)
2. Click the **Reload All Maven Projects** button (circular arrows icon)
3. **Build → Rebuild Project**

### Method 3: Add Generated Sources Manually

1. **File → Project Structure** (Cmd+; on Mac)
2. **Modules** → Select `eventstracker`
3. **Sources** tab
4. Click **+ Add Content Root**
5. Navigate to: `target/generated-sources/annotations`
6. Mark it as **Generated Sources Root** (blue folder)
7. **Apply** → **OK**

## 📝 What MapStruct Does

### Your Interface
```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DomainMapper {
    DomainDTO updateDomainDTO(Domain domain, @MappingTarget DomainDTO domainDTO);
    Domain updateDomain(DomainDTO domainDTO, @MappingTarget Domain domain);
}
```

### Generated Implementation
```java
@Component  // ← This is what Spring needs!
public class DomainMapperImpl implements DomainMapper {
    
    @Override
    public DomainDTO updateDomainDTO(Domain domain, DomainDTO domainDTO) {
        if (domain == null) {
            return domainDTO;
        }
        domainDTO.setDomainName(domain.getDomainName());
        domainDTO.setStatus(domain.getStatus());
        domainDTO.setComments(domain.getComments());
        // ... more mapping code
        return domainDTO;
    }
    
    @Override
    public Domain updateDomain(DomainDTO domainDTO, Domain domain) {
        // ... mapping code
    }
}
```

## 🔧 Troubleshooting

### Issue: "Generated files not found after compile"

**Solution 1**: Check Maven output for errors
```bash
mvn clean compile 2>&1 | tee compile.log
grep -i "error\|warning" compile.log
```

**Solution 2**: Verify annotation processors are configured
```bash
mvn help:effective-pom | grep -A 20 "annotationProcessorPaths"
```

### Issue: "Application still can't find DomainMapper"

**Checklist:**
- [ ] Did you run `mvn clean` before `mvn compile`?
- [ ] Check `target/generated-sources/annotations` exists
- [ ] Check `DomainMapperImpl.java` has `@Component` annotation
- [ ] Check `target/classes` contains `DomainMapperImpl.class`
- [ ] If using IntelliJ, did you reload Maven project?
- [ ] Try: **Build → Rebuild Project** in IntelliJ

### Issue: "Lombok and MapStruct conflict"

**Already Fixed!** Your pom.xml has:
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok-mapstruct-binding</artifactId>
    <version>0.2.0</version>
</dependency>
```

This ensures Lombok processes first, then MapStruct.

## ✅ Verify the Fix

### Command Line Verification
```bash
# 1. Compile
cd /Users/sathishjayapal/IdeaProjects/eventstracker
mvn clean compile

# 2. Check generated Java files
ls -la target/generated-sources/annotations/me/sathish/event_service/domain/

# 3. Check compiled class files
ls -la target/classes/me/sathish/event_service/domain/*MapperImpl.class

# 4. Verify it's a Spring component
grep "@Component" target/generated-sources/annotations/me/sathish/event_service/domain/DomainMapperImpl.java

# 5. Run the application
mvn spring-boot:run
```

### Expected Success Output
```
Started EventServiceApplication in X.XXX seconds
```

No more "DomainMapper bean not found" error! ✅

## 📚 Additional Resources

- [MapStruct Official Docs](https://mapstruct.org/)
- [Spring Boot with MapStruct](https://www.baeldung.com/mapstruct)
- [MapStruct + Lombok](https://mapstruct.org/documentation/stable/reference/html/#lombok)

## 🆘 Still Having Issues?

Run the diagnostic script:

```bash
cd /Users/sathishjayapal/IdeaProjects/eventstracker

# Full diagnostic
mvn clean compile -X 2>&1 | grep -i "mapstruct\|annotation" > mapstruct-debug.log
cat mapstruct-debug.log
```

Check:
1. Is `mapstruct-processor` in the annotation processor path?
2. Are `.java` files being generated?
3. Are `.class` files being compiled?

## 📁 Files I Created for You

1. **generate-mappers.sh** - Automated build and verification script
2. **MAPSTRUCT_FIX_README.md** - This file
3. **Updated pom.xml** - With MapStruct configuration

## 🎯 Summary

**The Problem**: MapStruct wasn't generating `DomainMapperImpl` class

**The Fix**: Updated `pom.xml` to properly configure annotation processing

**Next Step**: Run `mvn clean compile` and then `mvn spring-boot:run`

Your application should now start successfully! 🎉

