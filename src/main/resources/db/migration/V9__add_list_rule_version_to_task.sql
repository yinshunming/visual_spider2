alter table crawl_task
    add column list_rule_version_id bigint references crawl_rule_version(id);
