
# Etapas para o deploy

1. Buildando a imagem
```bash
docker build --platform linux/amd64 -t backend-eventostec:3.0 .
```

> Substituir o XX pela versão atual

2. Enviando para o Docker Hub
```bash
docker tag backend-eventostec:XX.0 kipperdev/backend-eventostec:XX.0
docker push kipperdev/backend-eventostec:XX.0
```

> Substituir o XX pela versão atual

3. Acessa máquina virtual

```bash
ssh ec2-user@44.212.51.2
```

4. Puxa e executa a imagem do Docker
```bash
docker pull kipperdev/backend-eventostec:XX.0
docker run -d -p 80:80 kipperdev/backend-eventostec:XX.0
```

> É importante mapear o Docker para a porta 80 da máquina virtual, pois é a porta que o LB está acessando.