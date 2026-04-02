import glob
import os

modules = {
    'product': {'seata': True, 'flyway': True, 'xxl': True, 'port': 9998},
    'order': {'seata': True, 'flyway': True, 'xxl': True, 'port': 9999},
    'ware': {'seata': True, 'flyway': True},
    'coupon': {'seata': True, 'flyway': True},
    'member': {'flyway': 'false'},
    'ai': {'flyway': 'false'},
    'seckill': {'xxl': True, 'port': 9997, 'sentinel_nacos': True},
    'gateway': {'sentinel_nacos': True}
}

for mod, flags in modules.items():
    path = f'd:/GitProgram/gulimail/gulimail-{mod}/src/main/resources/application.yml'
    if not os.path.exists(path): continue
    
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()
        
    if flags.get('flyway') == True and 'flyway:' not in content:
        flyway_cfg = '\n  flyway:\n    enabled: true\n    baseline-on-migrate: true\n    baseline-version: 1.0.0\n    baseline-description: "Initial version"'
        content = content.replace('spring:\n', 'spring:' + flyway_cfg + '\n')
    elif flags.get('flyway') == 'false' and 'flyway:' not in content:
        flyway_cfg = '\n  flyway:\n    enabled: false'
        content = content.replace('spring:\n', 'spring:' + flyway_cfg + '\n')

    if flags.get('xxl') and 'xxl:' not in content:
        port = flags['port']
        xxl_cfg = f'\nxxl:\n  job:\n    admin:\n      addresses: http://192.168.10.101:8080/xxl-job-admin\n    accessToken: default_token\n    executor:\n      appname: gulimail-{mod}-executor\n      port: {port}\n      logpath: ./logs/xxl-job/gulimail-{mod}\n      logretentiondays: 30\n'
        content += xxl_cfg

    if flags.get('seata') and 'seata:' not in content:
        seata_cfg = f'\nseata:\n  tx-service-group: gulimail-{mod}-tx-group\n  service:\n    vgroup-mapping:\n      gulimail-{mod}-tx-group: default\n  registry:\n    type: nacos\n    nacos:\n      server-addr: 127.0.0.1:8848\n      namespace: ""\n      group: SEATA_GROUP\n      application: seata-server\n'
        content += seata_cfg

    if flags.get('sentinel_nacos') and 'datasource:' not in content:
        if mod == 'seckill':
            sentinel_cfg = f'\n    sentinel:\n      transport:\n        dashboard: localhost:8333\n        port: 8719\n      datasource:\n        ds1:\n          nacos:\n            server-addr: 127.0.0.1:8848\n            dataId: gulimail-{mod}-sentinel-flow\n            groupId: DEFAULT_GROUP\n            data-type: json\n            rule-type: flow\n'
            if 'cloud:\n' in content:
                content = content.replace('cloud:\n', 'cloud:' + sentinel_cfg)

    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
    print(f'Reconstructed {path}')
