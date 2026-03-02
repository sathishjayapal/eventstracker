# Eventstracker Documentation

This single page consolidates the MapStruct quick fix and usage notes.

## MapStruct Bean Not Found – Quick Fix

**Symptoms**
```
Parameter 1 of constructor in me.sathish.event_service.domain.DomainService
required a bean of type 'me.sathish.event_service.domain.DomainMapper' that could not be found.
```

**Why it happens**
MapStruct implementations (e.g., `DomainMapperImpl`, `DomainEventMapperImpl`) were not generated/compiled, so Spring can’t find the beans.

**Fast fix commands**
```bash
cd /Users/sathishjayapal/IdeaProjects/eventstracker
mvn clean compile
mvn spring-boot:run
```

Or run the helper script:
```bash
./generate-mappers.sh
```

**Verify generation**
```bash
ls -la target/generated-sources/annotations/me/sathish/event_service/domain/
ls -la target/classes/me/sathish/event_service/domain/*MapperImpl.class
grep "@Component" target/generated-sources/annotations/me/sathish/event_service/domain/DomainMapperImpl.java
```

**IntelliJ IDEA setup**
- Settings → Build, Execution, Deployment → Compiler → Annotation Processors → ✅ Enable annotation processing
- Production sources: `target/generated-sources/annotations`
- Reload Maven projects, then Build → Rebuild Project

**Pom highlights already in place**
- MapStruct compiler args: `-Amapstruct.defaultComponentModel=spring` and `-Amapstruct.unmappedTargetPolicy=IGNORE`
- Lombok/MapStruct binding dependency
- Build helper adds generated sources

**Expected result**
- Generated files under `target/generated-sources/annotations/...`
- Application starts without “DomainMapper bean not found”

## Troubleshooting checklist
- Run `mvn clean compile` and check for errors/warnings.
- Ensure generated Java files exist in `target/generated-sources/annotations`.
- Ensure compiled classes exist in `target/classes/...MapperImpl.class`.
- If using IntelliJ, reload Maven and rebuild.
- For deep debug:
```bash
mvn clean compile -X 2>&1 | grep -i "mapstruct|annotation" > mapstruct-debug.log
cat mapstruct-debug.log
```

## Quick Reference (formerly QUICK_FIX.txt)
- Navigate: `cd /Users/sathishjayapal/IdeaProjects/eventstracker`
- Generate mappers: `mvn clean compile` (or `./generate-mappers.sh`)
- Run app: `mvn spring-boot:run`
- Generated files live in `target/generated-sources/annotations/...`
- If still stuck: `rm -rf target && mvn clean install -DskipTests`

All MapStruct guidance is now in this file to keep documentation in one place.

