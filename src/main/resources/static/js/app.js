(() => {
  'use strict';

  const STATUS_LABELS = { PENDING: 'Pendente', IN_PROGRESS: 'Em andamento', DONE: 'Concluída' };
  const FILTERS = [
    ['', 'Todas'],
    ['PENDING', 'Pendentes'],
    ['IN_PROGRESS', 'Em andamento'],
    ['DONE', 'Concluídas'],
  ];
  const PAGE_SIZE = 8;
  const TOKEN_KEY = 'taskapi.token';

  const $ = (selector) => document.querySelector(selector);

  const state = {
    token: readToken(),
    filter: '',
    page: 0,
    totalPages: 0,
    editingId: null,
    authMode: 'login',
  };

  // ---------- armazenamento do token ----------
  function readToken() {
    try { return localStorage.getItem(TOKEN_KEY); } catch { return null; }
  }
  function saveToken(token) {
    state.token = token;
    try {
      if (token) localStorage.setItem(TOKEN_KEY, token);
      else localStorage.removeItem(TOKEN_KEY);
    } catch { /* sem armazenamento: o login vale só até fechar a aba */ }
  }
  function emailFromToken(token) {
    try {
      const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(atob(payload)).sub || '';
    } catch { return ''; }
  }

  // ---------- conversa com a API ----------
  class ApiError extends Error {}

  async function api(path, { method = 'GET', body } = {}) {
    const headers = {};
    if (body) headers['Content-Type'] = 'application/json';
    if (state.token) headers['Authorization'] = 'Bearer ' + state.token;

    let response;
    try {
      response = await fetch(path, { method, headers, body: body ? JSON.stringify(body) : undefined });
    } catch {
      throw new ApiError('Não foi possível falar com o servidor. Ele está rodando?');
    }

    if (response.status === 401 && state.token) {
      logout();
      throw new ApiError('Sua sessão expirou. Entre novamente.');
    }
    if (response.status === 204) return null;

    const data = await response.json().catch(() => null);
    if (!response.ok) throw new ApiError(errorMessage(data, response.status));
    return data;
  }

  function errorMessage(data, status) {
    if (data && data.errors) return Object.values(data.errors).join(' · ');
    if (data && data.message) return data.message;
    return 'Algo deu errado (código ' + status + ').';
  }

  // ---------- ajudantes de tela ----------
  function el(tag, props = {}, ...children) {
    const node = document.createElement(tag);
    for (const [key, value] of Object.entries(props)) {
      if (key === 'class') node.className = value;
      else if (key === 'text') node.textContent = value;
      else if (key.startsWith('on')) node.addEventListener(key.slice(2), value);
      else node.setAttribute(key, value);
    }
    children.forEach((child) => child && node.append(child));
    return node;
  }

  let toastTimer;
  function toast(message, type = 'ok') {
    const box = $('#toast');
    box.textContent = message;
    box.className = 'toast' + (type === 'error' ? ' error' : '');
    box.hidden = false;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => { box.hidden = true; }, 3500);
  }

  function showError(selector, message) {
    const box = $(selector);
    box.textContent = message || '';
    box.hidden = !message;
  }

  function todayString() {
    const d = new Date();
    return d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
  }
  function formatDate(value) {
    const [year, month, day] = value.split('-');
    return day + '/' + month + '/' + year;
  }

  // ---------- entrada: login e cadastro ----------
  function setAuthMode(mode) {
    state.authMode = mode;
    const isLogin = mode === 'login';
    $('#name-field').hidden = isLogin;
    $('#auth-subtitle').textContent = isLogin ? 'Entre para ver suas tarefas.' : 'Crie sua conta em poucos segundos.';
    $('#auth-submit').textContent = isLogin ? 'Entrar' : 'Criar conta';
    $('#auth-switch-text').textContent = isLogin ? 'Ainda não tem conta?' : 'Já tem conta?';
    $('#auth-switch').textContent = isLogin ? 'Criar conta' : 'Entrar';
    $('#auth-password').autocomplete = isLogin ? 'current-password' : 'new-password';
    showError('#auth-error', '');
  }

  async function handleAuthSubmit(event) {
    event.preventDefault();
    showError('#auth-error', '');

    const email = $('#auth-email').value.trim();
    const password = $('#auth-password').value;
    const name = $('#auth-name').value.trim();

    if (!email || !password || (state.authMode === 'register' && !name)) {
      showError('#auth-error', 'Preencha todos os campos.');
      return;
    }

    const isLogin = state.authMode === 'login';
    const button = $('#auth-submit');
    button.disabled = true;
    try {
      const data = await api(isLogin ? '/auth/login' : '/auth/register', {
        method: 'POST',
        body: isLogin ? { email, password } : { name, email, password },
      });
      saveToken(data.token);
      $('#auth-form').reset();
      enterApp();
    } catch (error) {
      showError('#auth-error', error.message);
    } finally {
      button.disabled = false;
    }
  }

  function showAuth() {
    $('#app-view').hidden = true;
    $('#auth-view').hidden = false;
    setAuthMode('login');
  }

  function logout() {
    saveToken(null);
    showAuth();
  }

  function enterApp() {
    state.filter = '';
    state.page = 0;
    $('#auth-view').hidden = true;
    $('#app-view').hidden = false;
    $('#user-email').textContent = emailFromToken(state.token);
    renderFilters();
    loadTasks();
  }

  // ---------- lista de tarefas ----------
  function renderFilters() {
    const box = $('#filters');
    box.replaceChildren(...FILTERS.map(([value, label]) =>
      el('button', {
        type: 'button',
        text: label,
        'aria-pressed': String(state.filter === value),
        onclick: () => { state.filter = value; state.page = 0; renderFilters(); loadTasks(); },
      })));
  }

  async function loadTasks() {
    const list = $('#task-list');
    list.setAttribute('aria-busy', 'true');
    try {
      const params = new URLSearchParams({ page: state.page, size: PAGE_SIZE });
      if (state.filter) params.set('status', state.filter);
      const data = await api('/tasks?' + params);

      // Apagou o último item da página: volta uma página.
      if (data.content.length === 0 && data.page > 0) {
        state.page = Math.max(data.totalPages - 1, 0);
        return loadTasks();
      }
      state.totalPages = data.totalPages;
      renderTasks(data);
    } catch (error) {
      toast(error.message, 'error');
    } finally {
      list.removeAttribute('aria-busy');
    }
  }

  function renderTasks(data) {
    const list = $('#task-list');

    if (data.content.length === 0) {
      list.replaceChildren(el('li', { class: 'card empty', text: state.filter
        ? 'Nenhuma tarefa com esse status.'
        : 'Você ainda não tem tarefas. Clique em “Nova tarefa” para começar.' }));
    } else {
      list.replaceChildren(...data.content.map(taskCard));
    }

    $('#summary').textContent = data.totalElements === 1 ? '1 tarefa' : data.totalElements + ' tarefas';

    $('#pagination').hidden = data.totalPages <= 1;
    $('#page-info').textContent = 'Página ' + (data.page + 1) + ' de ' + data.totalPages;
    $('#prev').disabled = data.page === 0;
    $('#next').disabled = data.page + 1 >= data.totalPages;
  }

  function taskCard(task) {
    const overdue = task.dueDate && task.status !== 'DONE' && task.dueDate < todayString();

    const select = el('select', { 'aria-label': 'Status da tarefa', 'data-status': task.status },
      ...Object.entries(STATUS_LABELS).map(([value, label]) => {
        const option = el('option', { value, text: label });
        option.selected = value === task.status;
        return option;
      }));
    select.addEventListener('change', () => changeStatus(task.id, select.value));

    const meta = el('div', { class: 'task-meta' });
    if (task.dueDate) {
      meta.append(el('span', {
        class: 'chip' + (overdue ? ' overdue' : ''),
        text: (overdue ? 'Atrasada · ' : 'Prazo · ') + formatDate(task.dueDate),
      }));
    }

    return el('li', { class: 'card task', 'data-status': task.status },
      el('div', { class: 'task-head' }, el('h3', { class: 'task-title', text: task.title })),
      task.description ? el('p', { class: 'task-desc', text: task.description }) : null,
      meta.children.length ? meta : null,
      el('div', { class: 'task-actions' },
        select,
        el('span', { class: 'spacer' }),
        el('button', { class: 'btn ghost small', type: 'button', text: 'Editar', onclick: () => openDialog(task) }),
        el('button', { class: 'btn ghost small danger', type: 'button', text: 'Excluir', onclick: () => removeTask(task) })));
  }

  async function changeStatus(id, status) {
    try {
      await api('/tasks/' + id + '/status', { method: 'PATCH', body: { status } });
      toast('Status atualizado.');
    } catch (error) {
      toast(error.message, 'error');
    }
    loadTasks();
  }

  async function removeTask(task) {
    if (!confirm('Excluir a tarefa “' + task.title + '”?')) return;
    try {
      await api('/tasks/' + task.id, { method: 'DELETE' });
      toast('Tarefa excluída.');
    } catch (error) {
      toast(error.message, 'error');
    }
    loadTasks();
  }

  // ---------- criar e editar ----------
  function openDialog(task = null) {
    state.editingId = task ? task.id : null;
    $('#dialog-title').textContent = task ? 'Editar tarefa' : 'Nova tarefa';
    $('#task-title').value = task ? task.title : '';
    $('#task-description').value = task && task.description ? task.description : '';
    $('#task-due').value = task && task.dueDate ? task.dueDate : '';
    // Ao criar, não deixa escolher data no passado (a API também recusa).
    if (task) $('#task-due').removeAttribute('min');
    else $('#task-due').min = todayString();
    showError('#task-error', '');
    $('#task-dialog').showModal();
    $('#task-title').focus();
  }

  async function handleTaskSubmit(event) {
    event.preventDefault();
    showError('#task-error', '');

    const title = $('#task-title').value.trim();
    if (!title) {
      showError('#task-error', 'O título é obrigatório.');
      return;
    }

    const body = {
      title,
      description: $('#task-description').value.trim() || null,
      dueDate: $('#task-due').value || null,
    };

    try {
      if (state.editingId) {
        await api('/tasks/' + state.editingId, { method: 'PUT', body });
        toast('Tarefa atualizada.');
      } else {
        await api('/tasks', { method: 'POST', body });
        toast('Tarefa criada.');
        state.page = 0;
      }
      $('#task-dialog').close();
      loadTasks();
    } catch (error) {
      showError('#task-error', error.message);
    }
  }

  // ---------- ligações dos botões ----------
  $('#auth-form').addEventListener('submit', handleAuthSubmit);
  $('#auth-switch').addEventListener('click', () => setAuthMode(state.authMode === 'login' ? 'register' : 'login'));
  $('#logout').addEventListener('click', logout);
  $('#new-task').addEventListener('click', () => openDialog());
  $('#task-form').addEventListener('submit', handleTaskSubmit);
  $('#task-cancel').addEventListener('click', () => $('#task-dialog').close());
  $('#prev').addEventListener('click', () => { state.page = Math.max(state.page - 1, 0); loadTasks(); });
  $('#next').addEventListener('click', () => { state.page += 1; loadTasks(); });

  // ---------- início ----------
  if (state.token) enterApp();
  else showAuth();
})();
