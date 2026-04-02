import glob
import re

paths = glob.glob('d:/GitProgram/gulimail/gulimail-*/src/main/resources/application.yml')

for path in paths:
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Add graceful shutdown to server:
    if 'server:' in content and 'shutdown: graceful' not in content:
        content = re.sub(r'(server:.*?port: \d+)', r'\1\n  shutdown: graceful', content, flags=re.DOTALL)
        
    # Add lifecycle timeout to spring:
    if 'spring:' in content and 'timeout-per-shutdown-phase:' not in content:
        content = re.sub(r'^(spring:\s*\n)', r'\1  lifecycle:\n    timeout-per-shutdown-phase: 30s\n', content, count=1, flags=re.MULTILINE)
        
    # Add probes to management:
    if 'management:' in content and 'probes:' not in content:
        content = re.sub(r'^(management:\s*\n)', r'\1  endpoint:\n    health:\n      probes:\n        enabled: true\n  health:\n    livenessstate:\n      enabled: true\n    readinessstate:\n      enabled: true\n', content, count=1, flags=re.MULTILINE)
        
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)
        
    print(f'Updated probes for {path}')
