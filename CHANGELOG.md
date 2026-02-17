# Changelog

## [1.2.1](https://github.com/dnpm-dip/backend-core/compare/v1.2.0...v1.2.1) (2026-02-17)


### Bug Fixes

* Made deserialization of Patient.dateOfDeath LocalDate tolerant, to be backwards compatible with data containing only yyyy-MM ([88c78c4](https://github.com/dnpm-dip/backend-core/commit/88c78c4c26d677794f0c5d592d6a4665f19c16a4))

## [1.2.0](https://github.com/dnpm-dip/backend-core/compare/v1.1.4...v1.2.0) (2026-02-17)


### Features

* Added utility to complete a Coding with a Code prefix ([03800fc](https://github.com/dnpm-dip/backend-core/commit/03800fc5945ed3657d0d2890c433b14040164de0))


### Bug Fixes

* Reverted Patient.dateOfDeath to LocalDate to fulfil MV specs ([d3d61b3](https://github.com/dnpm-dip/backend-core/commit/d3d61b3959ab772cfd51c85c7cef4d572c7a6862))

## [1.1.4](https://github.com/dnpm-dip/backend-core/compare/v1.1.3...v1.1.4) (2025-11-13)


### Bug Fixes

* Corrected Reads[Patient] using fixed Reads[YearMonth] ([50df12c](https://github.com/dnpm-dip/backend-core/commit/50df12ceebe61cbf8e0b85f9f1be574f7d8d26cd))
* Corrected Reads[YearMonth] ([50df12c](https://github.com/dnpm-dip/backend-core/commit/50df12ceebe61cbf8e0b85f9f1be574f7d8d26cd))

## [1.1.3](https://github.com/dnpm-dip/backend-core/compare/v1.1.2...v1.1.3) (2025-11-07)


### Bug Fixes

* Changed Patient.birthDate and .dateOfDeath to YearMonth, but in a tolerant fashion, so that dates in JSON input would be converted to YearMonth ([97c44aa](https://github.com/dnpm-dip/backend-core/commit/97c44aa8bbd6ba4ac81824c5178db23fd08f9068))

## [1.1.2](https://github.com/dnpm-dip/backend-core/compare/v1.1.1...v1.1.2) (2025-10-09)


### Bug Fixes

* Fixed Json.Format[CodeSystem[_]] ([1d1bd66](https://github.com/dnpm-dip/backend-core/commit/1d1bd6603343101842f047d562aed5d65a71e5f2))

## [1.1.1](https://github.com/dnpm-dip/backend-core/compare/v1.1.0...v1.1.1) (2025-10-08)


### Bug Fixes

* Fixed CodeSystem JSON format ([5f23ee8](https://github.com/dnpm-dip/backend-core/commit/5f23ee8e55a90bbd05f9a744a288ab5739eb7c01))

## [1.1.0](https://github.com/dnpm-dip/backend-core/compare/v1.0.0...v1.1.0) (2025-10-08)


### Features

* Adapted CodeSystem to allow specifying a custom look-up function for concepts by code ([7c90343](https://github.com/dnpm-dip/backend-core/commit/7c9034377c6a63d83ab32a60bc17e0b4bfa56f8a))
* Added new ICD-10-GM property ([7c90343](https://github.com/dnpm-dip/backend-core/commit/7c9034377c6a63d83ab32a60bc17e0b4bfa56f8a))

## 1.0.0 (2025-08-05)


### Features

* Adapated cardinality on Diagnosis.recordedOn ([1bd2d30](https://github.com/dnpm-dip/backend-core/commit/1bd2d30262214de92162ebfebb77e854c8802e7a))
* Adaptation to PatientRecord to avoid explicitly optional CarePlans; Added Study.Registries.Unspecified ([9caf4f1](https://github.com/dnpm-dip/backend-core/commit/9caf4f1cbbf3b8e8c4b248cda15f62dfa9fe7ece))
* Adaptations to base model; Added some utilities ([d99c61b](https://github.com/dnpm-dip/backend-core/commit/d99c61bad927d6f1ebc6b1314bbfc6b4904fe29a))
* Added base trait for Chromosome; Added FollowUp.lastContactDate; Bit of code clean-up ([f5b5fa0](https://github.com/dnpm-dip/backend-core/commit/f5b5fa0b19eac3d1272c2b0646ced292e5dad3c7))
* Added convenience method to NGSReport base class; Upgraded Play JSON lib version ([bc80648](https://github.com/dnpm-dip/backend-core/commit/bc806489cf1f4cafa76480ae71696ce57d99fec5))
* Added convenience methods to History ([5c0b71d](https://github.com/dnpm-dip/backend-core/commit/5c0b71d2786053359b542047023c8d98400426f0))
* Added GeneAlterationReference to allow modelling which gene is the relevant one in the referenced variant object; fix: Corrected linter errors in Tests ([a067fb3](https://github.com/dnpm-dip/backend-core/commit/a067fb315bf557e07005cf3f60e77f67205dbb93))
* Added Retry utility for tasks requiring a retry strategy ([41bf2bc](https://github.com/dnpm-dip/backend-core/commit/41bf2bc98b9b645ff5dbe79588676b087f43ec31))
* Added useful (implicit) conversion methods to work with CodedEnum code values; some code clean-up ([2cae72b](https://github.com/dnpm-dip/backend-core/commit/2cae72bedc8c8b67dbbaa44eef719287c7ef8404))
* Added utility for better handling of Coding[System Union]; Further work on base model ([5bc6e34](https://github.com/dnpm-dip/backend-core/commit/5bc6e340bc308cb41e12c3056c0eb5f4065d097e))
* Corrected cardinality on Patient.address; Improvement to CarePlan.StatusReason handling ([ea9757f](https://github.com/dnpm-dip/backend-core/commit/ea9757fe901350a0a330b05c2b3d9528b887df4d))
* Defined type in Recommendation.supportingVariants reference ([764c237](https://github.com/dnpm-dip/backend-core/commit/764c237ef2ded8d2f22f5707cce2a0e494b0b561))
* Made Patient.address optional; Added specific JSON Schema for Address.MunicipalityCode; Some code clean-up ([7f0be1e](https://github.com/dnpm-dip/backend-core/commit/7f0be1e08cb361df0f777bb8556526e448df0da0))
* Refactored CarePlan.StatusReason to more explicit CarePlan.NoSequencingPerformedReason ([77b779b](https://github.com/dnpm-dip/backend-core/commit/77b779b7a88534485a46459f32e49941a34756ca))
* Upgraded dependency versions ([ea61ff8](https://github.com/dnpm-dip/backend-core/commit/ea61ff80a3c453aa72fbf223478def9948e44475))


### Bug Fixes

* Adapted Coding JSON serialization to avoid writing 'system' when implicitly defined ([66dc9e4](https://github.com/dnpm-dip/backend-core/commit/66dc9e4e9b3ef64aab7e12c17a6e76f0273f77b1))
* Adapted scalac linting and fixed many reported errors (mostly unused imports) ([795f875](https://github.com/dnpm-dip/backend-core/commit/795f875d97dffbbf4c6b6765eaf31de6af0438a9))
* Added better name derivation for Coding[Coproduct] ([f0b3406](https://github.com/dnpm-dip/backend-core/commit/f0b3406b9b8e3f4df8b59a6458e28a79a4355166))
* Added correct implementation of Coding.hashCode aligned with Coding.equals; Minor code clean-up ([ba5afe4](https://github.com/dnpm-dip/backend-core/commit/ba5afe42b4a50019e48300030cf25099a5ce7476))
* Added internal index to CodeSystems ([d5abfa4](https://github.com/dnpm-dip/backend-core/commit/d5abfa4bbaba2eb1fc24747e13e9bfc60fcb359b))
* Added new HealthInsuranceType 'SKT' ([58a61d2](https://github.com/dnpm-dip/backend-core/commit/58a61d230e04dd346e736e1a89378da624fd2d14))
* Corrected definition naming of Coding[Coproduct] ([e4c3f08](https://github.com/dnpm-dip/backend-core/commit/e4c3f0828befd08ca2c658c865cee376d6689734))
* Couple of little improvements ([db54570](https://github.com/dnpm-dip/backend-core/commit/db54570a8f3c30f9397e6e004307ded246387171))
* Fixed error filter regex for in ICD-O-3-T and -M codes ([868ff41](https://github.com/dnpm-dip/backend-core/commit/868ff412970cf94aa69afb3670c384795c825a94))
* Fixed JSON Schema for Patient to use specific Reference to HealthInsurance ([0db14c4](https://github.com/dnpm-dip/backend-core/commit/0db14c4b5cb8f2cbb746a5479474d93bed02686b))
* Fixed JSON Schema for YearMonth; code clean-up ([3bb6e17](https://github.com/dnpm-dip/backend-core/commit/3bb6e17bc40615943c7dbd165b3fc0a2661fb910))
* Fixed potential bug in Patient.ageOnDate; Fix to minor test ([78dbcce](https://github.com/dnpm-dip/backend-core/commit/78dbcce09d6e456375a3e8357ba8a9166af53f70))
* Minor clean-up ([bf56ba9](https://github.com/dnpm-dip/backend-core/commit/bf56ba929950057e17df94498175c9a35788b46f))
* Removed non-working Snyk workflow ([64bbaaf](https://github.com/dnpm-dip/backend-core/commit/64bbaaf74e2b7c4c5f05ba2efd136dd98f3f86c4))
* Reverted json.Schema[YearMonth] to 'LocalDate' ([69c443e](https://github.com/dnpm-dip/backend-core/commit/69c443e8f1fcec06e401ccfc56dd6b3447b306ce))
* Reverted removal of method CodeSystem.Concept.toCodingOf because this broke dependent code ([b17f05d](https://github.com/dnpm-dip/backend-core/commit/b17f05d0fe78994fc8a95f3c3e6bb968e0e37da4))
* Unified Chromosome value set to include chrMT, in accordance with MVH specs ([a80573f](https://github.com/dnpm-dip/backend-core/commit/a80573f086d525d39cb76cc7699992b119f7a9ab))
