#!/usr/bin/env python3
"""Generate MyBatis mapper/xml/repository from Jdbc*Repository sources."""

from __future__ import annotations

import re
import textwrap
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
INFRA_JAVA = ROOT / "pk-infra/src/main/java/com/pk/infra"
INFRA_RES = ROOT / "pk-infra/src/main/resources/mapper"
INSTANT = "com.pk.infra.mybatis.typehandler.InstantTypeHandler"
BOOL = "com.pk.infra.mybatis.typehandler.BooleanTinyintTypeHandler"

SKIP = set()


def domain(path: Path) -> str:
    return path.parts[path.parts.index("infra") + 1]


def entity(jdbc_stem: str) -> str:
    return jdbc_stem.removeprefix("Jdbc").removesuffix("Repository")


def port_fqn(java: str) -> str:
    m = re.search(r"implements\s+([\w.]+)", java)
    if not m:
        raise ValueError("no implements")
    return m.group(1)


def port_simple(port: str) -> str:
    return port.split(".")[-1]


def sql_blocks(java: str) -> list[str]:
    return [b.strip() for b in re.findall(r'"""\s*(.*?)\s*"""', java, re.DOTALL)]


def esc(sql: str) -> str:
    return sql.replace("<", "&lt;").replace(">", "&gt;")


def write(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8")


def gen_simple_insert_repo(domain_name: str, ent: str, port: str, jdbc_java: str, methods_meta: list) -> None:
    mapper_pkg = f"com.pk.infra.{domain_name}.mapper"
    repo_pkg = f"com.pk.infra.{domain_name}.repository"
    mapper = f"{ent}Mapper"
    repo = f"{ent}RepositoryImpl"
    mapper_fqn = f"{mapper_pkg}.{mapper}"
    blocks = sql_blocks(jdbc_java)

    # Build mapper interface from @Override methods
    iface_methods = []
    xml_parts = []
    repo_methods = []

    port_import = port if "." in port else f"com.pk.core.{domain_name}.port.{port}"

    # Parse override methods
    overrides = re.findall(
        r"@Override\s+(?:@\w+(?:\([^)]*\))?\s+)*public\s+([\w.<>,\s\[\]?]+?)\s+(\w+)\s*\(([^)]*)\)",
        jdbc_java,
        re.DOTALL,
    )

    sql_idx = 0
    for ret, name, params in overrides:
        ret = ret.strip()
        params = params.strip()
        param_names = []
        if params:
            for p in params.split(","):
                p = p.strip()
                pm = re.match(r"(?:final\s+)?[\w.<>,\s\[\]?]+\s+(\w+)$", p)
                if pm:
                    param_names.append(pm.group(1))

        # assign sql blocks heuristically
        sql = blocks[sql_idx] if sql_idx < len(blocks) else None
        sql_idx_increment = 0

        if name == "replaceContacts":
            # multi-sql method - handle in impl manually, skip auto
            continue
        if name == "replaceAll" or name == "replaceChildrenByParent" or name == "replaceSnapshots":
            continue
        if name == "upsertTerms":
            continue
        if name == "insert" and "List<" in params:
            continue
        if name == "insertBatch":
            continue
        if name == "findByProfileIdAndBillFilter":
            continue

        if sql is None:
            continue

        upper = sql.upper().lstrip()
        if upper.startswith("SELECT"):
            tag = "select"
            rt_attr = ""
            if ret.startswith("Optional"):
                inner = ret[ret.index("<") + 1 : ret.rindex(">")]
                if inner in {"Instant"}:
                    rt_attr = f' resultType="java.time.Instant"'
                elif inner.startswith("List"):
                    rt_attr = f' resultType="{inner.replace("<", "&lt;").replace(">", "&gt;")}"'
                else:
                    rt_attr = f' resultMap="{name}Map"'
            elif ret == "boolean" or ret == "Boolean":
                rt_attr = ' resultType="boolean"'
            elif ret in {"long", "int", "Integer", "Long"}:
                rt_attr = f' resultType="{ret.lower() if ret in {"long","int"} else "int"}"'
            elif ret.startswith("List"):
                rt_attr = f' resultMap="{name}Map"'
            else:
                rt_attr = f' resultMap="{name}Map"'
            xml_parts.append(f'    <{tag} id="{name}"{rt_attr}>\n        {esc(sql)}\n    </{tag}>')
            sql_idx_increment = 1
        elif upper.startswith("INSERT"):
            tag = "insert"
            use_keys = ""
            if ret in {"long", "Long"} or "GeneratedKey" in jdbc_java[max(0, jdbc_java.find(sql)-200):jdbc_java.find(sql)+len(sql)+200]:
                use_keys = ' useGeneratedKeys="true" keyProperty="id" keyColumn="id"'
            xml_parts.append(f'    <{tag} id="{name}"{use_keys}>\n        {esc(sql)}\n    </{tag}>')
            sql_idx_increment = 1
        else:
            tag = "update" if upper.startswith("UPDATE") else "delete" if upper.startswith("DELETE") else "update"
            xml_parts.append(f'    <{tag} id="{name}">\n        {esc(sql)}\n    </{tag}>')
            sql_idx_increment = 1

        # mapper signature
        if params:
            param_ann = ", ".join(f"@Param(\"{n}\") {t}" for n, t in zip(param_names, ["Object"] * len(param_names)))
            # simplified - use @Param only
            sig_params = ", ".join(
                f'@Param("{n}") {infer_type(n, sql, jdbc_java)} {n}' for n in param_names
            )
        else:
            sig_params = ""

        if ret == "void":
            iface_methods.append(f"    void {name}({sig_params});")
        else:
            iface_methods.append(f"    {ret} {name}({sig_params});")

        sql_idx += sql_idx_increment

    mapper_java = textwrap.dedent(
        f"""
        package {mapper_pkg};

        import org.apache.ibatis.annotations.Mapper;
        import org.apache.ibatis.annotations.Param;

        @Mapper
        public interface {mapper} {{
        {chr(10).join(iface_methods) if iface_methods else "    // see XML"}
        }}
        """
    ).strip() + "\n"

    xml = (
        '<?xml version="1.0" encoding="UTF-8" ?>\n'
        '<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"\n'
        '        "https://mybatis.org/dtd/mybatis-3-mapper.dtd">\n'
        f'<mapper namespace="{mapper_fqn}">\n\n'
        + "\n\n".join(xml_parts)
        + "\n</mapper>\n"
    )

    write(INFRA_JAVA / domain_name / "mapper" / f"{mapper}.java", mapper_java)
    write(INFRA_RES / domain_name / f"{mapper}.xml", xml)

    repo_java = textwrap.dedent(
        f"""
        package {repo_pkg};

        import {port_import};
        import {mapper_fqn};
        import org.springframework.stereotype.Repository;

        @Repository
        public class {repo} implements {port_simple(port_import)} {{
            private final {mapper} mapper;

            public {repo}({mapper} mapper) {{
                this.mapper = mapper;
            }}

            // TODO: implement port methods by delegating to mapper (migrate from Jdbc source)
        }}
        """
    ).strip() + "\n"
    write(INFRA_JAVA / domain_name / "repository" / f"{repo}.java", repo_java)


def infer_type(name: str, sql: str, java: str) -> str:
    if "Instant" in java and name.endswith("At"):
        return "java.time.Instant"
    if name == "profileId" or name.endswith("Id") and "long" in java:
        return "long"
    if name == "limit":
        return "int"
    return "String"


def main() -> None:
    for jdbc_path in sorted(INFRA_JAVA.rglob("Jdbc*Repository.java")):
        if jdbc_path.name in SKIP:
            continue
        java = jdbc_path.read_text(encoding="utf-8")
        d = domain(jdbc_path)
        ent = entity(jdbc_path.stem)
        port = port_fqn(java)
        print(f"stub: {d}/{ent}")
        try:
            gen_simple_insert_repo(d, ent, port, java, [])
        except Exception as e:
            print(f"  skip {jdbc_path.name}: {e}")


if __name__ == "__main__":
    main()
