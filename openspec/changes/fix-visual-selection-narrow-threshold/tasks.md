## 1. Implementation

> Implementation completed prior to OpenSpec documentation.

- [x] 1.1 Modify widthPercent threshold: 0.6 → 0.2
- [x] 1.2 Modify heightPercent threshold: 0.18 → 0.08
- [x] 1.3 Modify area threshold: 260000 → 400000
- [x] 1.4 Modify div text.length limit: 90 → 200
- [x] 1.5 Modify div childCount limit: 5 → 12

## 2. Verification

- [x] 2.1 Run `mvn compile` - Build succeeds
- [x] 2.2 Run `mvn test` - All tests pass

## 3. Manual Verification (Pending)

- [ ] 3.1 Start application: `mvn spring-boot:run`
- [ ] 3.2 Create preview session for https://www.sina.com.cn
- [ ] 3.3 Verify selectable elements count increased
- [ ] 3.4 Verify news items in page are now clickable

## 4. Documentation

- [x] 4.1 Create proposal.md
- [x] 4.2 Create design.md
- [x] 4.3 Create specs/visual-selection-threshold/spec.md
- [x] 4.4 Create tasks.md
