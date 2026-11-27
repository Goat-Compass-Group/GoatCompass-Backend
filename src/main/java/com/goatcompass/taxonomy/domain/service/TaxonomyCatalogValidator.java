package com.goatcompass.taxonomy.domain.service;

import com.goatcompass.taxonomy.domain.model.TaxonomyCatalog;
import com.goatcompass.taxonomy.domain.model.TaxonomyCodes;
import com.goatcompass.taxonomy.domain.model.aggregate.Credential;
import com.goatcompass.taxonomy.domain.model.aggregate.JobFamily;
import com.goatcompass.taxonomy.domain.model.aggregate.Occupation;
import com.goatcompass.taxonomy.domain.model.aggregate.Skill;
import com.goatcompass.taxonomy.domain.model.enumeration.CredentialRequirement;
import com.goatcompass.taxonomy.domain.model.enumeration.RelationType;
import com.goatcompass.taxonomy.domain.model.enumeration.Status;
import com.goatcompass.taxonomy.domain.model.valueobject.ExternalReference;
import com.goatcompass.taxonomy.domain.model.valueobject.SkillRelation;
import com.goatcompass.taxonomy.domain.model.valueobject.TaxonomyCode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

public final class TaxonomyCatalogValidator {

    private TaxonomyCatalogValidator() {
    }

    public static Result validate(TaxonomyCatalog catalog, TaxonomyCodes existing) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        Set<TaxonomyCode> families = codes(catalog.jobFamilies(), JobFamily::code, "lĩnh vực", errors);
        Set<TaxonomyCode> skills = codes(catalog.skills(), Skill::getCode, "kỹ năng", errors);
        Set<TaxonomyCode> credentials = codes(catalog.credentials(), Credential::code, "chứng chỉ", errors);
        Set<TaxonomyCode> occupations = codes(catalog.occupations(), Occupation::getCode, "nghề", errors);

        checkSkills(catalog.skills(), skills, errors);
        checkCredentials(catalog.credentials(), skills, credentials, errors);
        checkOccupations(catalog.occupations(), families, skills, credentials, occupations, errors);
        checkExternalReferences(catalog.externalReferences(), skills, credentials, occupations, errors);
        checkCycles(catalog.skills(), RelationType.BROADER, errors);
        checkCycles(catalog.skills(), RelationType.PREREQUISITE, errors);
        checkRemoved("lĩnh vực", existing.jobFamilies(), families, errors);
        checkRemoved("kỹ năng", existing.skills(), skills, errors);
        checkRemoved("chứng chỉ", existing.credentials(), credentials, errors);
        checkRemoved("nghề", existing.occupations(), occupations, errors);

        warnAmbiguousAliases(catalog.skills(), warnings);
        warnUnusedSkills(catalog, warnings);
        return new Result(errors, warnings);
    }

    private static <T> Set<TaxonomyCode> codes(List<T> items, Function<T, TaxonomyCode> code, String type,
                                               List<String> errors) {
        Set<TaxonomyCode> seen = new HashSet<>();
        for (T item : items) {
            if (!seen.add(code.apply(item))) {
                errors.add("Mã " + type + " bị trùng: " + code.apply(item));
            }
        }
        return seen;
    }

    private static void checkSkills(List<Skill> skills, Set<TaxonomyCode> skillCodes, List<String> errors) {
        Map<Set<TaxonomyCode>, Integer> relatedPairs = new HashMap<>();
        for (Skill s : skills) {
            String where = "Kỹ năng " + s.getCode();
            for (SkillRelation r : s.getRelations()) {
                requireRef(where + " (" + r.type() + ")", r.target(), skillCodes, errors);
                if (r.target().equals(s.getCode())) {
                    errors.add(where + ": quan hệ " + r.type() + " trỏ vào chính nó");
                }
                if (r.type() == RelationType.RELATED) {
                    relatedPairs.merge(Set.of(s.getCode(), r.target()), 1, Integer::sum);
                }
            }
            checkStatus(where, s.getStatus(), s.getReplacedBy(), s.getCode(), skillCodes, errors);
        }
        relatedPairs.forEach((pair, count) -> {
            if (count > 1) {
                errors.add("Quan hệ RELATED giữa " + new TreeSet<>(pair.stream().map(TaxonomyCode::value).toList())
                        + " khai báo ở cả hai phía (chỉ khai báo một phía, chiều ngược được sinh tự động)");
            }
        });
    }

    private static void checkCredentials(List<Credential> credentials, Set<TaxonomyCode> skillCodes,
                                         Set<TaxonomyCode> credentialCodes, List<String> errors) {
        for (Credential c : credentials) {
            String where = "Chứng chỉ " + c.code();
            Set<TaxonomyCode> seen = new HashSet<>();
            for (Credential.EvidencedSkill e : c.skills()) {
                requireRef(where, e.skill(), skillCodes, errors);
                if (!seen.add(e.skill())) {
                    errors.add(where + ": kỹ năng " + e.skill() + " liệt kê hai lần");
                }
            }
            checkStatus(where, c.status(), c.replacedBy(), c.code(), credentialCodes, errors);
        }
    }

    private static void checkOccupations(List<Occupation> occupations, Set<TaxonomyCode> familyCodes,
                                         Set<TaxonomyCode> skillCodes, Set<TaxonomyCode> credentialCodes,
                                         Set<TaxonomyCode> occupationCodes, List<String> errors) {
        for (Occupation o : occupations) {
            String where = "Nghề " + o.getCode();
            o.getJobFamilies().forEach(f -> requireRef(where + " (lĩnh vực)", f, familyCodes, errors));
            Set<TaxonomyCode> seenSkills = new HashSet<>();
            for (Occupation.SkillRequirement r : o.getSkills()) {
                requireRef(where, r.skill(), skillCodes, errors);
                if (!seenSkills.add(r.skill())) {
                    errors.add(where + ": kỹ năng " + r.skill() + " liệt kê hai lần");
                }
            }
            Set<TaxonomyCode> seenCredentials = new HashSet<>();
            for (Occupation.CredentialRequirementLink c : o.getCredentials()) {
                requireRef(where, c.credential(), credentialCodes, errors);
                if (!seenCredentials.add(c.credential())) {
                    errors.add(where + ": chứng chỉ " + c.credential() + " liệt kê hai lần");
                }
                if (c.requirement() == CredentialRequirement.LEGALLY_REQUIRED
                        && (c.noteVi() == null || c.noteVi().isBlank())) {
                    errors.add(where + ": chứng chỉ " + c.credential()
                            + " là LEGALLY_REQUIRED nên phải có note_vi (bắt buộc cho hoạt động nào)");
                }
            }
            checkStatus(where, o.getStatus(), o.getReplacedBy(), o.getCode(), occupationCodes, errors);
        }
    }

    private static void checkExternalReferences(List<ExternalReference> refs, Set<TaxonomyCode> skills,
                                                Set<TaxonomyCode> credentials, Set<TaxonomyCode> occupations,
                                                List<String> errors) {
        Set<ExternalReference> seen = new HashSet<>();
        for (ExternalReference r : refs) {
            Set<TaxonomyCode> targets = switch (r.entityType()) {
                case SKILL -> skills;
                case CREDENTIAL -> credentials;
                case OCCUPATION -> occupations;
            };
            requireRef("Liên kết " + r.scheme() + " " + r.externalCode(), r.entityCode(), targets, errors);
            if (!seen.add(r)) {
                errors.add("Liên kết " + r.scheme() + " " + r.externalCode() + " của " + r.entityCode() + " bị trùng");
            }
        }
    }

    /** DEPRECATED phải trỏ sang mục thay thế có thật; trạng thái khác thì không được trỏ. */
    private static void checkStatus(String where, Status status, TaxonomyCode replacedBy, TaxonomyCode self,
                                    Set<TaxonomyCode> sameType, List<String> errors) {
        if (status == Status.DEPRECATED) {
            if (replacedBy == null) {
                errors.add(where + ": DEPRECATED phải có replaced_by");
            } else {
                requireRef(where + " (replaced_by)", replacedBy, sameType, errors);
                if (replacedBy.equals(self)) {
                    errors.add(where + ": replaced_by trỏ vào chính nó");
                }
            }
        } else if (replacedBy != null) {
            errors.add(where + ": chỉ mục DEPRECATED mới có replaced_by");
        }
    }

    private static void checkCycles(List<Skill> skills, RelationType type, List<String> errors) {
        Map<TaxonomyCode, List<TaxonomyCode>> graph = new LinkedHashMap<>();
        skills.forEach(s -> graph.put(s.getCode(), s.related(type)));
        Map<TaxonomyCode, Integer> state = new HashMap<>(); // 1 = đang duyệt, 2 = xong
        for (TaxonomyCode start : graph.keySet()) {
            if (!state.containsKey(start)) {
                dfs(start, graph, state, new ArrayList<>(List.of(start)), type, errors);
            }
        }
    }

    private static void dfs(TaxonomyCode node, Map<TaxonomyCode, List<TaxonomyCode>> graph,
                            Map<TaxonomyCode, Integer> state, List<TaxonomyCode> path, RelationType type,
                            List<String> errors) {
        state.put(node, 1);
        for (TaxonomyCode next : graph.getOrDefault(node, List.of())) {
            if (state.get(next) == null) {
                path.add(next);
                dfs(next, graph, state, path, type, errors);
                path.removeLast();
            } else if (state.get(next) == 1) {
                List<TaxonomyCode> cycle = new ArrayList<>(path.subList(path.indexOf(next), path.size()));
                cycle.add(next);
                errors.add("Chu trình " + type + ": " + String.join(" → ", cycle.stream().map(TaxonomyCode::value).toList()));
            }
        }
        state.put(node, 2);
    }

    private static void checkRemoved(String type, Set<TaxonomyCode> existing, Set<TaxonomyCode> incoming,
                                     List<String> errors) {
        existing.stream().filter(c -> !incoming.contains(c)).sorted((a, b) -> a.value().compareTo(b.value()))
                .forEach(c -> errors.add("Mã " + type + " " + c + " đang có trong kho nhưng không còn trong nguồn."
                        + " Không xoá mã: hãy giữ lại và đánh dấu DEPRECATED + replaced_by"));
    }

    private static void requireRef(String where, TaxonomyCode ref, Set<TaxonomyCode> known, List<String> errors) {
        if (!known.contains(ref)) {
            errors.add(where + ": tham chiếu tới mã không tồn tại '" + ref + "'");
        }
    }

    private static void warnAmbiguousAliases(List<Skill> skills, List<String> warnings) {
        Map<String, Set<String>> owners = new LinkedHashMap<>();
        skills.forEach(s -> s.getAliases().forEach(a ->
                owners.computeIfAbsent(a.normalized(), k -> new TreeSet<>()).add(s.getCode().value())));
        owners.forEach((alias, codes) -> {
            if (codes.size() > 1) {
                warnings.add("Alias '" + alias + "' thuộc nhiều kỹ năng " + codes + " (bước LLM sẽ chọn theo ngữ cảnh)");
            }
        });
    }

    private static void warnUnusedSkills(TaxonomyCatalog catalog, List<String> warnings) {
        Set<TaxonomyCode> used = new HashSet<>();
        catalog.occupations().forEach(o -> o.getSkills().forEach(r -> used.add(r.skill())));
        catalog.credentials().forEach(c -> c.skills().forEach(e -> used.add(e.skill())));
        catalog.skills().forEach(s -> s.getRelations().forEach(r -> used.add(r.target())));
        catalog.skills().stream().map(Skill::getCode).filter(c -> !used.contains(c))
                .forEach(c -> warnings.add("Kỹ năng " + c + " chưa được nghề, chứng chỉ hay kỹ năng nào tham chiếu"));
    }

    /** Kết quả kiểm tra: còn lỗi thì không được ghi. */
    public record Result(List<String> errors, List<String> warnings) {

        public Result {
            errors = List.copyOf(errors);
            warnings = List.copyOf(warnings);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }
    }
}
